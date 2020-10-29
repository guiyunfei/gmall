package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author guiban
 * @email guiban@atguigu.com
 * @date 2020-10-28 16:54:41
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}
