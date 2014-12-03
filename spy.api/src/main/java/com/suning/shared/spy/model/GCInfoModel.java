/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: GCInfoModel.java
 * Author:   13073050
 * Date:     2014年10月30日 下午2:16:58
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.shared.spy.model;

import com.suning.shared.spy.enums.MetricsType;

/**
 * 〈一句话功能简述〉<br>
 * 〈功能详细描述〉
 * 
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class GCInfoModel extends JmxMetricsInfoModel {

    /**
     */
    private static final long serialVersionUID = -2143214111340522519L;
    
    public double youngTime; //young gc总时间
    public double fullgcTime;  //full gc总时间
    public double youngGCConut;//young gc次数
    public double fullGCCount;//full gc次数
    
    public GCInfoModel() {
        // TODO Auto-generated constructor stub
    }
    
    public double getYoungGCConut() {
        return youngGCConut;
    }
    
    public double getFullGCCount() {
        return fullGCCount;
    }
    
    public double getYoungTime() {
        return youngTime;
    }

    public double getFullgcTime() {
        return fullgcTime;
    }

    public enum GCConnector {

        PAR_NEW("ParNew"), 
        CMS("ConcurrentMarkSweep"), 
        PS_SCAVENGE("PS Scavenge"), 
        PS_MARKSWEEP("PS MarkSweep");

        private String name;

        GCConnector(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
    /*
     * (non-Javadoc)
     * @see com.suning.shared.spy.model.MetricsInfoModel#getType()
     */
    @Override
    public MetricsType getType() {
        return MetricsType.META_GC;
    }
    
    ///////////////////write////////////////
    public void setYoungTime(double youngTime) {
        this.youngTime = youngTime;
    }

    public void setFullgcTime(double fullgcTime) {
        this.fullgcTime = fullgcTime;
    }

    public void setYoungGCConut(double youngGCConut) {
        this.youngGCConut = youngGCConut;
    }

    public void setFullGCCount(double fullGCCount) {
        this.fullGCCount = fullGCCount;
    }
    
    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setPidName(String pidName) {
        this.pidName = pidName;
    }
    public void setTarget(boolean target) {
        this.target = target;
    }

}
