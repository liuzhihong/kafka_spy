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

import java.util.List;

import org.apache.log4j.Logger;

import com.suning.app.spy.core.command.AbstractCommandHandler;
import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.model.CmdResult;
import com.suning.shared.spy.model.VmMemInfoModel;

/**
 * 〈一句话功能简述〉获取系统内存信息
 *
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class VmMemoryInfoComandImpl extends AbstractCommandHandler<VmMemInfoModel> {
    
    private static final Logger LOGGER = Logger.getLogger(VmMemoryInfoComandImpl.class); 
    @Override
    public VmMemInfoModel doHandle(String[] cmd) {
        if (null == cmd || cmd.length < 1) {
            LOGGER.warn("cmdArray is empty!");
            return null;
        }
        LOGGER.info("start excute the command : "+cmd);
        VmMemInfoModel memoryInfo = new VmMemInfoModel();
        CmdResult cmdResult = getCmdOperateService().execute(cmd);
        List<String> comResults = cmdResult.getCommonResList();
        if(comResults != null && comResults.size() > 0){
            for(String result : comResults){
                String [] data = result.split("\\s+");
                if(result.startsWith("MemTotal")){
                    memoryInfo.setTotalMem(Long.parseLong(data[1])/1024);
                }
                if(result.startsWith("MemFree")){
                    memoryInfo.setFreeMem(Long.parseLong(data[1])/1024);
                }
                if(result.startsWith("Buffers")){
                    memoryInfo.setBufferdMem(Long.parseLong(data[1])/1024);
                }
                if(result.startsWith("Cached")){
                    memoryInfo.setCachedMem(Long.parseLong(data[1])/1024);
                }
                if(result.startsWith("SwapCached")){
                    memoryInfo.setSwapCached(Long.parseLong(data[1])/1024);
                }
            }
        }
        return memoryInfo;
    }

    @Override
    public String getCommandName() {
        return Constants.METRICS_MEM;
    }

}
