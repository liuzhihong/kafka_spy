/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: NetInfo.java
 * Author:   13073050
 * Date:     2014年10月28日 下午9:42:52
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.shared.spy.model;

import com.suning.shared.spy.enums.MetricsType;

/**
 *  网络信息参数vo类
 *  linux命令：cat /proc/net/dev
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class NetInfoModel extends MetricsInfoModel{
    /**
     */
    private static final long serialVersionUID = 4097045214826747671L;
    public long receiveByte;//网络接收字节
    public long transmitByte;//网络传送字节 
    public double receiveRate;//网卡接收速率 KB/s
    public double transmitRate;//网卡传送速率 KB/s
    
    public long getReceiveByte() {
        return receiveByte;
    }
    
    public long getTransmitByte() {
        return transmitByte;
    }
    
    public double getReceiveRate() {
        return receiveRate;
    }

    public double getTransmitRate() {
        return transmitRate;
    }

    @Override
    public MetricsType getType() {
        return MetricsType.NET;
    }
    
    ///////////////////////////write////////////////////////
    public void setReceiveByte(long receiveByte) {
        this.receiveByte = receiveByte;
    }
    public void setTransmitByte(long transmitByte) {
        this.transmitByte = transmitByte;
    }
    public void setReceiveRate(double receiveRate) {
        this.receiveRate = receiveRate;
    }
    public void setTransmitRate(double transmitRate) {
        this.transmitRate = transmitRate;
    }
}