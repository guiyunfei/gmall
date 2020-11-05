package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.api.GmallSmsClient;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.pms.vo.SupAttrValueVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuInfo(PageParamVo pageParamVo, Long categoryId) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        if (categoryId != 0) {
            wrapper.eq("category_id", categoryId);
        }
        //如果有检索条件key查询条件
        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)) {
            //函数式变成之消费器
            wrapper.and(t -> {
                t.like("name", key).or().like("id", key);
            });
        }
        return new PageResultVo(this.page(pageParamVo.getPage(), wrapper));
    }

    @Autowired
    SpuDescMapper spuDescMapper;
    @Autowired
    SpuAttrValueService spuAttrValueService;

    @Autowired
    SkuMapper skuMapper;

    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuAttrValueService skuAttrValueService;
    @Autowired
    GmallSmsClient gmallSmsClient;

   //本地事物 @Transactional(propagation = Propagation.REQUIRED)
    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spu) {
        //1保存spu相关的
        //1.0 spu 的基本信息
        Long spuId = saveSpu(spu);

        //1.1保存spu的描述信息
        saveSpuDesc(spu, spuId);

        //1.2保存spu的规格参数信息
        saveBaseAttr(spu, spuId);

        //2保存sku相关的
        saveSkus(spu, spuId);

        int a =1/0;
    }

    private void saveSkus(SpuVo spu, Long spuId) {
        List<SkuVo> skuVos = spu.getSkus();
        if (CollectionUtils.isEmpty(skuVos)) {
            return;
        }
        //sku的基本信息
        skuVos.forEach(skuVo -> {
            skuVo.setSpuId(spuId);
            skuVo.setBrandId(spu.getBrandId());
            skuVo.setCatagoryId(spu.getCategoryId());
            List<String> images = skuVo.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                skuVo.setDefaultImage(StringUtils.isNotBlank(skuVo.getDefaultImage()) ? skuVo.getDefaultImage() : images.get(0));
            }
            skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();
            //sku的图片信息
            if (!CollectionUtils.isEmpty(images)) {
                List<SkuImagesEntity> skuImagesEntitys = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(skuVo.getDefaultImage(), image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntitys);
            }
            //sku的规格参数
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            saleAttrs.forEach(sale -> sale.setSkuId(skuId));
            skuAttrValueService.saveBatch(saleAttrs);
            //3保存营销相关的远程调用gmall-sms
                 //  int a=1/0;
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            gmallSmsClient.saveSkuSaleInfo(skuSaleVo);
        });
    }

    private void saveBaseAttr(SpuVo spu, Long spuId) {
        List<SupAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            //map映射需要转换器 把一个对象转换另外的对象新流
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(i -> {
                i.setSort(0);
                i.setSpuId(spuId);
                return i;
            }).collect(Collectors.toList());
            spuAttrValueService.saveBatch(spuAttrValueEntities);
        }
    }

    private void saveSpuDesc(SpuVo spu, Long spuId) {
        SpuDescEntity spuDescEntity = new SpuDescEntity();
        List<String> spuImages = spu.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)) {
            String join = StringUtils.join(spuImages, ",");
            spuDescEntity.setDecript(join);
            spuDescEntity.setSpuId(spuId);
            spuDescMapper.insert(spuDescEntity);
        }
    }

    private Long saveSpu(SpuVo spu) {
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        return spu.getId();
    }


   /* public static void main(String[] args) {
        //map  filter reduce
        List<User> users = Arrays.asList(
                new User("小明", 22, true),
                new User("小黑", 42, false),
                new User("小白", 52, true),
                new User("小路", 12, true)
        );
        //过滤filter
        //users.stream().filter(User::getGender).forEach(System.err::println);
        //map集合转换 user->person
        List<Person> personList = users.stream().map(user -> {
            Person person = new Person();
            person.setUserName(user.getName());
            person.setAge(user.getAge());
            return person;
        }).collect(Collectors.toList());
        //   personList.forEach(System.err::println);

        //求和:reduce  函数式接口 调用 R apply(T t, U u);
       Integer integer = users.stream().map(User::getAge).reduce((a,b)->a+b).get();
    //    System.out.println(integer);


    }*/


}

/*
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class User {
    String name;
    Integer age;
    Boolean gender;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class Person {
    String userName;
    Integer age;
}*/
