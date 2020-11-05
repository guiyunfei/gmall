package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface GmallPmsApi {
    @PostMapping("pms/spu/json")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);



    @GetMapping("pms/sku/spu/{supId}")
    public ResponseVo<List<SkuEntity>> querySkuEntityBySpuId(@PathVariable Long supId);


    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);


    @GetMapping("pms/spuattrvalue/search/attr/{spuId}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchSpuValueByCidAndSpuId(
            @RequestParam("cid") Long cid, @PathVariable Long spuId
    );

    @GetMapping("pms/sku/search/attr/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchSkuValueByCidAndSpuId(
            @RequestParam("cid") Long cid, @PathVariable Long skuId
    );

}