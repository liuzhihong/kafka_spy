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

import java.text.DecimalFormat;
import java.util.List;

import org.apache.log4j.Logger;

import com.suning.app.spy.core.command.AbstractCommandHandler;
import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.model.CmdResult;
import com.suning.shared.spy.model.NetInfoModel;

/**
 * 获取网络相关信息
 * 统计一段时间内Receive和Tramsmit的bytes数的变化，单位大B
 * 即可获得网口传输速率，再除以网口的带宽就得到带宽的使用率
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class NETInfoComandImpl extends AbstractCommandHandler<NetInfoModel> {
    
    private NetInfoModel prevInfoModel;
    private static final Logger LOGGER = Logger.getLogger(NETInfoComandImpl.class); 
    @Override
    public NetInfoModel doHandle(String[] cmd) {
        if (null == cmd || cmd.length < 1) {
            LOGGER.warn("cmdArray is empty!");
            return null;
        }
        LOGGER.info("start excute the command : "+cmd);
        NetInfoModel netInfo = new NetInfoModel();
        CmdResult cmdResult = getCmdOperateService().execute(cmd);
        List<String> comResults = cmdResult.getCommonResList();
        if(comResults != null && comResults.size() > 0){
            for(String result : comResults){
                result = result.trim();
                if(result.startsWith("eth0")){
                    LOGGER.info(result);
                    String [] data = result.split("\\s+");
                    netInfo.setReceiveByte(Long.parseLong(data[0].substring(5)));//Receive bytes,单位为Byte
                    netInfo.setTransmitByte(Long.parseLong(data[8]));//Transmit bytes,单位为Byte
                    break;
                }
            }
        }
        NetInfoModel retNetInfoModel = new NetInfoModel();
        if(prevInfoModel == null){
            prevInfoModel = netInfo;
            return null;
        }else{
            retNetInfoModel.setReceiveByte(netInfo.getReceiveByte());
            retNetInfoModel.setTransmitByte(netInfo.getTransmitByte());
            retNetInfoModel.setReceiveRate(parseDataToReceiveRate(prevInfoModel,netInfo));
            retNetInfoModel.setTransmitRate(parseDataToTransRate(prevInfoModel,netInfo));
        }
        prevInfoModel = netInfo;
        return retNetInfoModel;
    }
    /**
     * 
     * 功能描述: 计算网卡传送速率
     *
     * @param prevInfoModel2
     * @param netInfo
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private double parseDataToTransRate(NetInfoModel prevInfoModel2, NetInfoModel netInfo) {
        long t1 = netInfo.getTransmitByte()-prevInfoModel2.getTransmitByte();
        double temp = t1*1.0/((netInfo.getCollectTime().getTime()-prevInfoModel2.getCollectTime().getTime())/1000);
        double retTransRate = formatData(temp);
        LOGGER.info("当前网卡传送速率: "+retTransRate+"KB/s");
        return retTransRate;
    }
    
    /**
     * 
     * 功能描述: 计算网卡接收速率
     *
     * @param prevInfoModel2
     * @param netInfo
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private double parseDataToReceiveRate(NetInfoModel prevInfoModel2, NetInfoModel netInfo) {
        long t1 = netInfo.getReceiveByte()-prevInfoModel2.getReceiveByte();
        double temp = t1*1.0/((netInfo.getCollectTime().getTime()-prevInfoModel2.getCollectTime().getTime())/1000);
        double retReceiveRate = formatData(temp);
        LOGGER.info("当前网卡接收速率: "+retReceiveRate+"KB/s");
        return retReceiveRate;
    }

    private double formatData(double temp) {
        DecimalFormat df = new DecimalFormat("#.##");
        String retData = df.format(temp/1024);
        return Double.parseDouble(retData);
    }
    @Override
    public String getCommandName() {
        return Constants.METRICS_NET;
    }

}
