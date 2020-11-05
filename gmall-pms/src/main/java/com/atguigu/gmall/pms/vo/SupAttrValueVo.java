package com.atguigu.gmall.pms.vo;

import com.alibaba.nacos.client.utils.StringUtils;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;
@Data
public class SupAttrValueVo extends SpuAttrValueEntity {

  //  private List<String> valueSelected;
    //上面属性可以删掉 调用的本质就是调用set方法
    public void setValueSelected(List<String> valueSelected){
        if(CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        setAttrValue(StringUtils.join(valueSelected,","));
    }
}
