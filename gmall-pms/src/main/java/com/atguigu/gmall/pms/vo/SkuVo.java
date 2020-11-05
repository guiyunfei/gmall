package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuVo extends SkuEntity {

    private List<String> images;

    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    /**
     * 满几件
     * 打几折
     */
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;


    /**
     * 满多少
     * 减多少
     */
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    private List<SkuAttrValueEntity> saleAttrs;

}
