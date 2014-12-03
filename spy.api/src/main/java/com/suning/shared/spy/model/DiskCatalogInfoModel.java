/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: DiskCatalogInfoModel.java
 * Author:   13073050
 * Date:     2014年11月12日 上午11:32:28
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.shared.spy.model;

import java.io.Serializable;

/**
 * 〈一句话功能简述〉每个挂载目录的具体信息 ，大小为MB
 *
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class DiskCatalogInfoModel implements Serializable{
	private static final long serialVersionUID = -4287012653692437308L;
	protected double catalogSize;//目录空间
    protected double catalogUsed;//已使用空间
    protected double catalogAvailable;//可用空间
    protected double catalogUsage;//使用率
    
    public double getCatalogSize() {
        return catalogSize;
    }
    public double getCatalogUsed() {
        return catalogUsed;
    }
    public double getCatalogAvailable() {
        return catalogAvailable;
    }
    public double getCatalogUsage() {
        return catalogUsage;
    }
    
    ////write//////////////////
    public void setCatalogSize(double catalogSize) {
        this.catalogSize = catalogSize;
    }
    public void setCatalogUsed(double catalogUsed) {
        this.catalogUsed = catalogUsed;
    }
    public void setCatalogAvailable(double catalogAvailable) {
        this.catalogAvailable = catalogAvailable;
    }
    public void setCatalogUsage(double catalogUsage) {
        this.catalogUsage = catalogUsage;
    }
    
}
