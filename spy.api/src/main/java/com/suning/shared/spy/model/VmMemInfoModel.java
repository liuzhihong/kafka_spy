/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: SystemInfo.java
 * Author:   13073050
 * Date:     2014年10月28日 上午11:55:54
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.shared.spy.model;

import java.text.DecimalFormat;

import com.suning.shared.spy.enums.MetricsType;

/**
 * 〈功能详细描述〉
 *  linux命令:cat /proc/meminfo
 *  默认单位为KB
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class VmMemInfoModel extends MetricsInfoModel{
    /**
     */
    private static final long serialVersionUID = -8050550421778156028L;
    public long totalMem;//总的物理内存
    public long freeMem;//可用内存
    public long bufferdMem;
    public long cachedMem;
    public long swapCached;
    
    
    public long getTotalMem() {
        return totalMem;
    }

    public long getFreeMem() {
        return freeMem;
    }

    public long getBufferdMem() {
        return bufferdMem;
    }

    public long getCachedMem() {
        return cachedMem;
    }
    
    public long getSwapCached() {
        return swapCached;
    }
    
    @Override
    public MetricsType getType() {
        return MetricsType.MEM;
    }
    
    /**
     * 
     * 功能描述: <br>
     * 计算内存的利用率
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public String getMemUsage(){
        double memUsage = 1-(double)this.freeMem*1.0/(double)this.totalMem;
        return new DecimalFormat("0.00").format(memUsage);
    }
    
    ////////////////////write/////////////////////
    public void setTotalMem(long totalMem) {
        this.totalMem = totalMem;
    }
    public void setFreeMem(long freeMem) {
        this.freeMem = freeMem;
    }
    public void setBufferdMem(long bufferdMem) {
        this.bufferdMem = bufferdMem;
    }
    public void setCachedMem(long cachedMem) {
        this.cachedMem = cachedMem;
    }
    public void setSwapCached(long swapCached) {
        this.swapCached = swapCached;
    }
    
}