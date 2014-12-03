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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.suning.app.spy.core.command.AbstractCommandHandler;
import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.model.CmdResult;
import com.suning.shared.spy.model.CpuInfoModel;
import com.suning.shared.spy.model.CpuInfoModel.CpuModel;

/**
 * 〈一句话功能简述〉获取系统CPU信息
 *
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class CPUInfoComandImpl extends AbstractCommandHandler<CpuInfoModel> {
    
    private static final Logger LOGGER = Logger.getLogger(CPUInfoComandImpl.class); 
    private CpuInfoModel prevInfoModel; //保存上一个CPU模型的数据
    @Override
    public CpuInfoModel doHandle(String[] cmd) {
        if (null == cmd || cmd.length < 1) {
            LOGGER.warn("cmdArray is empty!");
            return null;
        }
        LOGGER.info("start excute the command : "+cmd);
        //当前的CPU模型
        CpuInfoModel cpuModelInfo = new CpuInfoModel();
        List<CpuModel> cpuList = new ArrayList<CpuModel>();
        CmdResult cmdResult = getCmdOperateService().execute(cmd);
        List<String> comResultList = cmdResult.getCommonResList();
        if(comResultList != null && comResultList.size() > 0){
            for(String result : comResultList){
                result = result.trim();
                LOGGER.info(result);
                Matcher m = Pattern.compile("^[1-9][0-9]*$").matcher(result);
                if(m.find()){
                    cpuModelInfo.setCpuNumber(Integer.parseInt(m.group()));
                }
                if(result.indexOf("load") != -1){
                    float [] loadValue = parseFloatArry(result);
                    cpuModelInfo.setLoadValue(loadValue);
                }
                if(result.startsWith("cpu")){
                    //第一个放进去的始终是总的CPU信息
                    String [] data = result.split("\\s+");
                    CpuModel cpuModel = new CpuModel();
                    cpuModel.setUserTime(Long.parseLong(data[1]));
                    cpuModel.setNiceTime(Long.parseLong(data[2]));
                    cpuModel.setSysTime(Long.parseLong(data[3]));
                    cpuModel.setIdleTime(Long.parseLong(data[4]));
                    cpuModel.setIowaitTime(Long.parseLong(data[5]));
                    cpuModel.setIrqTime(Long.parseLong(data[6]));
                    cpuModel.setSoftirqTime(Long.parseLong(data[7]));
                    cpuModel.setStealstolenTime(Long.parseLong(data[8]));
                    cpuModel.setGuestTime(Long.parseLong(data[9]));
                    cpuModel.setTotalCpuTime(sumTotalCpuTime(cpuModel));
                    cpuList.add(cpuModel);
                }
            }
            cpuModelInfo.setCpuList(cpuList);
        }
        //要返回的CPU模型
        CpuInfoModel retCpuInfoModel = new CpuInfoModel();
        retCpuInfoModel.setCpuNumber(cpuModelInfo.getCpuNumber());
        retCpuInfoModel.setLoadValue(cpuModelInfo.getLoadValue());
        List<CpuModel> cpus = new ArrayList<CpuInfoModel.CpuModel>();
        if(prevInfoModel == null){
            prevInfoModel = cpuModelInfo;
            return null;
        } else {
            for(int index=0;index<prevInfoModel.getCpuList().size();index++){
                CpuModel model = new CpuModel();
                //存放两次CPU采样之间的时间差值
                CpuModel currentCpuModel = cpuModelInfo.getCpuList().get(index);
                CpuModel preCpuModel = prevInfoModel.getCpuList().get(index);
                model.setUserTime(currentCpuModel.getUserTime()-preCpuModel.getUserTime());
                model.setNiceTime(currentCpuModel.getNiceTime()-preCpuModel.getNiceTime());
                model.setSysTime(currentCpuModel.getSysTime()-preCpuModel.getSysTime());
                model.setIdleTime(currentCpuModel.getIdleTime()-preCpuModel.getIdleTime());
                model.setIowaitTime(currentCpuModel.getIowaitTime()-preCpuModel.getIowaitTime());
                model.setIrqTime(currentCpuModel.getIrqTime()-preCpuModel.getIrqTime());
                model.setSoftirqTime(currentCpuModel.getSoftirqTime()-preCpuModel.getSoftirqTime());
                model.setStealstolenTime(currentCpuModel.getStealstolenTime()-preCpuModel.getStealstolenTime());
                model.setGuestTime(currentCpuModel.getGuestTime()-preCpuModel.getGuestTime());
                model.setTotalCpuTime(currentCpuModel.getTotalCpuTime()-preCpuModel.getTotalCpuTime());
                cpus.add(model);
            }
        }
        retCpuInfoModel.setCpuList(cpus);
        //用当前的CPU模型覆盖上一次采样的CPU模型
        prevInfoModel = cpuModelInfo;
        return retCpuInfoModel;
    }
    /**
     * 
     * 功能描述: 计算总的CPU时间
     *
     * @param cpuModel
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private long sumTotalCpuTime(CpuModel cpuModel) {
        return cpuModel.getUserTime()+cpuModel.getNiceTime()+cpuModel.getSysTime()+cpuModel.getIdleTime()
                +cpuModel.getIowaitTime()+cpuModel.getIrqTime()+cpuModel.getSoftirqTime()+cpuModel.getStealstolenTime()
                +cpuModel.getGuestTime();
    }

    private float[] parseFloatArry(String result) {
        float [] loadValue = new float[3];
        String [] temp = result.split("\\s+");
        //分别代表前一分钟，五分钟，十五分钟的平均CPU负载
        loadValue[0] = Float.parseFloat(temp[temp.length-3].substring(0, temp[temp.length-3].indexOf(",")));
        loadValue[1] = Float.parseFloat(temp[temp.length-2].substring(0, temp[temp.length-2].indexOf(",")));
        loadValue[2] = Float.parseFloat(temp[temp.length-1]);
        return loadValue;
    }

    @Override
    public String getCommandName() {
        return Constants.METRICS_CPU;
    }
    public static void main(String[] args) {
        String str = "17:51:47 up 92 days, 21:46,  2 users,  load average: 1.00, 2.00, 3.00";
        String [] strs = str.split("\\s+");
        System.out.println(strs.length);
        String data1 = strs[9].substring(0, strs[9].indexOf(","));
        System.out.println(data1);
        String data2 = strs[10].substring(0, strs[10].indexOf(","));
        System.out.println(data2);
        String data3 = strs[11];
        System.out.println(data3);
    }
}
