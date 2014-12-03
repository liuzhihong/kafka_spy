/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: ListHeapInfoModel.java
 * Author:   13073050
 * Date:     2014年11月7日 上午9:13:48
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.shared.spy.model;

import java.util.Map;

import com.suning.shared.spy.enums.MetricsType;

/**
 * 〈一句话功能简述〉<br> 
 * 〈功能详细描述〉
 *
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class AggregateHeapInfoModel extends MetricsInfoModel {

    /**
     */
    private static final long serialVersionUID = -6049004190569539524L;
    
    //TODO Key改为appName
    public Map<String, HeapInfoModel> extHeapInfo;
    
    //TODO 监控自身
    public HeapInfoModel selfHeapInfo;
    //监控目标对象
    public HeapInfoModel targetHeapInfo;
    
    

    public Map<String, HeapInfoModel> getExtHeapInfo() {
        return extHeapInfo;
    }

    public HeapInfoModel getTargetHeapInfo() {
        return targetHeapInfo;
    }

    public HeapInfoModel getSelfHeapInfo() {
        return selfHeapInfo;
    }

    @Override
    public MetricsType getType() {
        return MetricsType.HEAP;
    }
    
    /////////////////////write///////////////////
    public void setTargetHeapInfo(HeapInfoModel targetHeapInfo) {
        this.targetHeapInfo = targetHeapInfo;
    }
    
    public void setExtHeapInfo(Map<String, HeapInfoModel> extHeapInfo) {
        this.extHeapInfo = extHeapInfo;
    }
    
    public void setSelfHeapInfo(HeapInfoModel selfHeapInfo) {
        this.selfHeapInfo = selfHeapInfo;
    }

}
