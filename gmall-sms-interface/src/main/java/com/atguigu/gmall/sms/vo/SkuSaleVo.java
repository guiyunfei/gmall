package com.atguigu.gmall.sms.vo;

import com.oracle.webservices.internal.api.databinding.DatabindingMode;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuSaleVo {

    private Long skuId;
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
    /**
     * 是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
    private Integer ladderAddOther;


    /**
     * 满多少
     * 减多少
     */
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;
}
