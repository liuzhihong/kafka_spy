/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: ListGCInfoModel.java
 * Author:   13073050
 * Date:     2014年11月7日 上午9:41:45
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
public class AggregateGCInfoModel extends MetricsInfoModel {

    /**
     */
    private static final long serialVersionUID = -2320921593809922236L;

    //TODO Key改为MetricsTarget
    public Map<String, GCInfoModel> extGcInfo;
    
    //TODO 监控自身    
    public GCInfoModel selfGcInfo;
    //监控目标对象
    public GCInfoModel targetGcInfo;
    
    
    
    public Map<String, GCInfoModel> getExtGcInfo() {
        return extGcInfo;
    }

    public GCInfoModel getTargetGcInfo() {
        return targetGcInfo;
    }

    public GCInfoModel getSelfGcInfo() {
        return selfGcInfo;
    }

    /* (non-Javadoc)
     * @see com.suning.shared.spy.model.MetricsInfoModel#getType()
     */
    @Override
    public MetricsType getType() {
        return MetricsType.GC;
    }
    
    //////////////////////write////////////////////
    public void setExtGcInfo(Map<String, GCInfoModel> extGcInfo) {
        this.extGcInfo = extGcInfo;
    }

    public void setTargetGcInfo(GCInfoModel targetGcInfo) {
        this.targetGcInfo = targetGcInfo;
    }
    
    public void setSelfGcInfo(GCInfoModel selfGcInfo) {
        this.selfGcInfo = selfGcInfo;
    }

}
