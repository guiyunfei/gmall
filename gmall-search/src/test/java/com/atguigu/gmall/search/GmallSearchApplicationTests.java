package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    GoodsRepository goodsRepository;

    @Autowired
    ElasticsearchRestTemplate restTemplate;

    @Autowired
    GmallWmsClient gmallWmsClient;

    @Autowired
    GmallPmsClient gmallPmsClient;

    @Test
    void contextLoads() {
        this.restTemplate.createIndex(Goods.class);
        this.restTemplate.putMapping(Goods.class);

        Integer pageNum = 1;
        Integer pageSize = 100;
        do {

            PageParamVo pageParamVo = new PageParamVo();
            pageParamVo.setPageNum(pageNum);
            pageParamVo.setPageSize(pageSize);
            ResponseVo<List<SpuEntity>> listResponseVo = gmallPmsClient.querySpuByPageJson(pageParamVo);
            //分页查询spu
            List<SpuEntity> data = listResponseVo.getData();
            if (CollectionUtils.isEmpty(data)) {
                break;
            }
            data.forEach(spuEntity -> {
                List<SkuEntity> skuEntities = gmallPmsClient.querySkuEntityBySpuId(spuEntity.getId()).getData();
                if (!CollectionUtils.isEmpty(skuEntities)) {
                    List<Goods> collect = skuEntities.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        //sku相关
                        goods.setSkuId(skuEntity.getId());
                        goods.setTitle(skuEntity.getTitle());
                        goods.setSubTitle(skuEntity.getSubtitle());
                        goods.setPrice(skuEntity.getPrice().doubleValue());
                        goods.setDefaultImage(skuEntity.getDefaultImage());
                        //spu相关
                        goods.setCreateTime(spuEntity.getCreateTime());

                        //查询库存相关 销量和bool货
                        ResponseVo<List<WareSkuEntity>> listResponseVo2 = gmallWmsClient.queryWareEntityBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntityList = listResponseVo2.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntityList)) {
                            //anyMatch只要有一个就true
                            goods.setStore(wareSkuEntityList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                            //销量redius
                            goods.setSales(wareSkuEntityList.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                        }

                        //查询品牌
                        BrandEntity brandEntity = gmallPmsClient.queryBrandById(skuEntity.getBrandId()).getData();
                        if (brandEntity != null) {
                            goods.setBrandId(brandEntity.getId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }
                        //查询分类
                        CategoryEntity categoryEntity = gmallPmsClient.queryCategoryById(skuEntity.getCatagoryId()).getData();
                        if (categoryEntity != null) {
                            goods.setCategoryId(categoryEntity.getId());
                            goods.setCategoryName(categoryEntity.getName());
                        }
                        //查询规格参数/
                        //销量类型的检索规格参数和值
                        List<SearchAttrValue> searchAttrValues = new ArrayList<>();

                        List<SkuAttrValueEntity> skuAttrValueEntityList = gmallPmsClient.querySearchSkuValueByCidAndSpuId(skuEntity.getCatagoryId(), skuEntity.getId()).getData();

                        if (!CollectionUtils.isEmpty(skuAttrValueEntityList)) {
                            searchAttrValues.addAll(skuAttrValueEntityList.stream().map(skuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValue);
                                return searchAttrValue;
                            }).collect(Collectors.toList()));

                        }
                        //基本类型的检索规格参数和值
                        ResponseVo<List<SpuAttrValueEntity>> listResponseVo1 = gmallPmsClient.querySearchSpuValueByCidAndSpuId(spuEntity.getCategoryId(), spuEntity.getId());
                        List<SpuAttrValueEntity> spuAttrValueEntityList = listResponseVo1.getData();
                        if (spuAttrValueEntityList!=null) {
                            searchAttrValues.addAll(spuAttrValueEntityList.stream().map(spuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValue);
                                return searchAttrValue;
                            }).collect(Collectors.toList()));
                        }
                        goods.setSearchAttrs(searchAttrValues);
                        return goods;
                    }).collect(Collectors.toList());

                    goodsRepository.saveAll(collect);

                }


            });


            pageSize = data.size();
            pageNum++;
        } while (pageSize == 100);//当商品不满100的时候就退出循环

    }



}
