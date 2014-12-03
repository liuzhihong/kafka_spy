/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: SystemInfoComandImpl.java
 * Author:   13073050
 * Date:     2014年10月28日 上午10:56:47
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.app.spy.core.command.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.suning.app.spy.core.command.AbstractCommandHandler;
import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.model.CmdResult;
import com.suning.shared.spy.model.DiskCatalogInfoModel;
import com.suning.shared.spy.model.DiskInfoModel;

/**
 * 〈一句话功能简述〉获取磁盘信息
 * 
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class DiskInfoComandImpl extends AbstractCommandHandler<DiskInfoModel> {

    private static final Logger LOGGER = Logger.getLogger(DiskInfoComandImpl.class);

    @Override
    public DiskInfoModel doHandle(String[] cmd) {
        if (null == cmd || cmd.length < 1) {
            LOGGER.warn("cmdArray is empty!");
            return null;
        }
        LOGGER.info("start excute the command : " + cmd);
        CmdResult cmdResult = getCmdOperateService().execute(cmd);
        List<String> comResults = cmdResult.getCommonResList();
        float ioUsage = 0.0f;
        int count = 0;
        DiskInfoModel diskInfo = new DiskInfoModel();
        Map<String,DiskCatalogInfoModel> catalogs = new HashMap<String,DiskCatalogInfoModel>();
        boolean physicDisk = true;
        if(comResults != null && comResults.size() > 0){
            for(String result : comResults){
                if(++count >= 4){
                    String [] data = result.split("\\s+");
                    if(result.startsWith("vda") && physicDisk){
                        float readKBPerSecond = Float.parseFloat(data[5]);
                        float writeKBPerSecond = Float.parseFloat(data[6]);
                        diskInfo.setReadSpeed(readKBPerSecond);
                        diskInfo.setWriteSpeed(writeKBPerSecond);
                        physicDisk = false;
                    }
                    if(data.length > 1 && data[data.length-1].matches("[0-9]+\\.[0-9]+")){  
                        float util =  Float.parseFloat(data[data.length-1]);  
                        ioUsage = (ioUsage>util)?ioUsage:util;  
                    }
                    if(data[data.length-1].startsWith("/") && data.length > 1){
                        DiskCatalogInfoModel catalogInfoModel = new DiskCatalogInfoModel();
                        catalogInfoModel.setCatalogUsage(parseUsageValue(data[data.length-2]));
                        catalogInfoModel.setCatalogAvailable(Double.parseDouble(data[data.length-3]));
                        catalogInfoModel.setCatalogUsed(Double.parseDouble(data[data.length-4]));
                        catalogInfoModel.setCatalogSize(Double.parseDouble(data[data.length-5]));
                        catalogs.put(data[data.length-1], catalogInfoModel);
                    }
                }
            }
        }
        diskInfo.setCatalogs(catalogs);  
        diskInfo.setIoUsage(ioUsage);
        return diskInfo;
    }

    private double parseUsageValue(String str) {
        double usage = Double.parseDouble(str.split("%")[0])/100;
        return usage;
    }

    @Override
    public String getCommandName() {
        return Constants.METRICS_DISK;
    }

}
