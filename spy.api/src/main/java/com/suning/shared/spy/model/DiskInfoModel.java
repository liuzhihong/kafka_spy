/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: DiskInfo.java
 * Author:   13073050
 * Date:     2014年10月28日 下午9:13:10
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
 *  linux命令：iostat -d -k -x && df -hm
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class DiskInfoModel extends MetricsInfoModel{
    /**
     */
    private static final long serialVersionUID = 8540986833982850310L;
    public Map<String, DiskCatalogInfoModel> catalogs;
    public float ioUsage;
    public float readSpeed;  //KB/s
    public float writeSpeed;     //KB/s
    
    
    
    public Map<String, DiskCatalogInfoModel> getCatalogs() {
        return catalogs;
    }

    public float getIoUsage() {
        return ioUsage;
    }
    
    public float getReadSpeed() {
        return readSpeed;
    }

    public float getWriteSpeed() {
        return writeSpeed;
    }
    
    @Override
    public MetricsType getType() {
        return MetricsType.DISK;
    }
    
    ///////////////////write////////////////////
    public void setCatalogs(Map<String, DiskCatalogInfoModel> catalogs) {
        this.catalogs = catalogs;
    }
    
    public void setIoUsage(float ioUsage) {
        this.ioUsage = ioUsage;
    }
    
    public void setReadSpeed(float readSpeed) {
        this.readSpeed = readSpeed;
    }

    public void setWriteSpeed(float writeSpeed) {
        this.writeSpeed = writeSpeed;
    }

}