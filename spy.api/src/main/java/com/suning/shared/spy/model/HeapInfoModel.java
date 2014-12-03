/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: MemoryInfoModel.java
 * Author:   13073050
 * Date:     2014年10月31日 上午10:27:32
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
 *  单位都是字节 1024byte = 1KB
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class HeapInfoModel extends JmxMetricsInfoModel {
    
    /**
     */
    private static final long serialVersionUID = -1302483975864759518L;
    /**
     * JVM堆 内存总量
     */
    public double totalMem;
    /**
     * 当前分配量
     */
    public double commitMem;
    /**
     * 当前使用量
     */
    public double usedMem;
    /**
     * 最大Eden区分配大小
     */
    public double maxEdenMem;
    /**
     * 初始化Eden大小
     */
    public double initEdenMem;
    /**
     * Eden区已分配的大小
     */
    public double edenCommitMem;
    /**
     * Eden区已使用大小
     */
    public double edenUsedMem;
    /**
     * 最大Surviror区大小
     */
    public double maxSurvirorMem;
    /**
     *  初始化Surviror区大小
     */
    public double initSurvirorMem;
    /**
     * Surviror区已分配大小
     */
    public double survirorCommitMem;
    /**
     * Surviror区已使用大小
     */
    public double survirorUsedMem;
    /**
     * 最大老年代分配大小
     */
    public double maxOldMem;
    /**
     * 初始化老年代大小
     */
    public double initOldMem;
    /**
     * 老年代分配大小
     */
    public double oldCommitMem;
    /**
     * 老年代使用大小
     */
    public double oldUsedMem;
    /**
     * 最大永久代大小
     */
    public double maxPermMem;
    /**
     * 初始化永久代大小
     */
    protected double initPermMem;
    /**
     * 永久代分配大小
     */
    protected double permCommitMem;
    /**
     * 永久代使用大小
     */
    public double permUsedMem;
    
    public double getTotalMem() {
        return totalMem;
    }

    public double getCommitMem() {
        return commitMem;
    }

    public double getUsedMem() {
        return usedMem;
    }

    public double getEdenCommitMem() {
        return edenCommitMem;
    }

    public double getEdenUsedMem() {
        return edenUsedMem;
    }

    public double getSurvirorCommitMem() {
        return survirorCommitMem;
    }

    public double getSurvirorUsedMem() {
        return survirorUsedMem;
    }

    public double getOldCommitMem() {
        return oldCommitMem;
    }

    public double getOldUsedMem() {
        return oldUsedMem;
    }

    public double getPermCommitMem() {
        return permCommitMem;
    }

    public double getPermUsedMem() {
        return permUsedMem;
    }

    public double getMaxEdenMem() {
        return maxEdenMem;
    }

    public double getInitEdenMem() {
        return initEdenMem;
    }

    public double getMaxSurvirorMem() {
        return maxSurvirorMem;
    }

    public double getInitSurvirorMem() {
        return initSurvirorMem;
    }

    public double getMaxOldMem() {
        return maxOldMem;
    }

    public double getInitOldMem() {
        return initOldMem;
    }

    public double getMaxPermMem() {
        return maxPermMem;
    }

    public double getInitPermMem() {
        return initPermMem;
    }


    public enum JvmType {
        
        //并行方式内存代区分配
        PS_EDEN_SPACE("PS Eden Space"),
        PS_SURVIROR_SPACE("PS Survivor Space"),
        PS_OLD_GEN("PS Old Gen"),
        PS_PERM_GEN("PS Perm Gen"),
        
        //并发方式代区分配
        PAR_EDEN_SPACE("Par Eden Space"),
        PAR_SURVIROR_SPACE("Par Surviror Space"),
        CMS_OLD_GEN("CMS Old Gen"),
        CMS_PERM_GEN("CMS Perm Gen");

        private String name;

        JvmType(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
    public enum HeapType {
        EDEN("Eden"),
        SURVIROR("Surviror"),
        PERM("Perm"),
        OLD("Old");
        
        private String name;
        private HeapType(String name) {
            this.name = name;
        }
        public String getName() {
            return this.name;
        }
    }
    /* (non-Javadoc)
     * @see com.suning.shared.spy.model.MetricsInfoModel#getType()
     */
    @Override
    public MetricsType getType() {
        
        return MetricsType.META_HEAP;
    }
    
    /////////////////write///////////////////
    public void setTotalMem(double totalMem) {
        this.totalMem = totalMem;
    }

    public void setCommitMem(double commitMem) {
        this.commitMem = commitMem;
    }

    public void setUsedMem(double usedMem) {
        this.usedMem = usedMem;
    }

    public void setEdenCommitMem(double edenCommitMem) {
        this.edenCommitMem = edenCommitMem;
    }

    public void setEdenUsedMem(double edenUsedMem) {
        this.edenUsedMem = edenUsedMem;
    }

    public void setSurvirorCommitMem(double survirorCommitMem) {
        this.survirorCommitMem = survirorCommitMem;
    }

    public void setSurvirorUsedMem(double survirorUsedMem) {
        this.survirorUsedMem = survirorUsedMem;
    }

    public void setOldCommitMem(double oldCommitMem) {
        this.oldCommitMem = oldCommitMem;
    }

    public void setOldUsedMem(double oldUsedMem) {
        this.oldUsedMem = oldUsedMem;
    }

    public void setPermCommitMem(double permCommitMem) {
        this.permCommitMem = permCommitMem;
    }

    public void setPermUsedMem(double permUsedMem) {
        this.permUsedMem = permUsedMem;
    }
    public void setMaxEdenMem(double maxEdenMem) {
        this.maxEdenMem = maxEdenMem;
    }

    public void setInitEdenMem(double initEdenMem) {
        this.initEdenMem = initEdenMem;
    }

    public void setMaxSurvirorMem(double maxSurvirorMem) {
        this.maxSurvirorMem = maxSurvirorMem;
    }

    public void setInitSurvirorMem(double initSurvirorMem) {
        this.initSurvirorMem = initSurvirorMem;
    }

    public void setMaxOldMem(double maxOldMem) {
        this.maxOldMem = maxOldMem;
    }

    public void setInitOldMem(double initOldMem) {
        this.initOldMem = initOldMem;
    }

    public void setMaxPermMem(double maxPermMem) {
        this.maxPermMem = maxPermMem;
    }

    public void setInitPermMem(double initPermMem) {
        this.initPermMem = initPermMem;
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
