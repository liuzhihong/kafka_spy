/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: JmxOperateServiceCopy.java
 * Author:   13073050
 * Date:     2014年11月14日 上午9:31:03
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.app.spy.core.service.impl;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.suning.app.spy.core.AbstractLifeCycle;
import com.suning.app.spy.core.Configuration;
import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.service.Service;
import com.suning.app.spy.core.service.ServiceControl;
import com.suning.app.spy.core.support.JmxClient;
import com.suning.app.spy.core.support.JmxClient.ConnectionState;
import com.suning.app.spy.core.utils.MachineInfoUtil;
import com.suning.app.spy.core.utils.StringHelper;
import com.suning.shared.spy.model.AppMetricsKeyModel;
import com.suning.shared.spy.model.GCInfoModel;
import com.suning.shared.spy.model.HeapInfoModel;
import com.suning.shared.spy.model.GCInfoModel.GCConnector;
import com.suning.shared.spy.model.HeapInfoModel.HeapType;

/**
 * 〈一句话功能简述〉<br>
 * 〈功能详细描述〉
 * 
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class JmxOperateService extends AbstractLifeCycle implements Service {

    private static final Logger LOGGER = Logger.getLogger(JmxOperateService.class);
    private Map<AppMetricsKeyModel, JmxClient> jmxClients = new HashMap<AppMetricsKeyModel, JmxClient>();
    private CmdOperateService cmdOperateService = ServiceControl.getInstance().getBean(CmdOperateService.class);

    @Override
    protected void doStart(Configuration conf) throws Exception {
        Map<String, GCInfoModel> modelMap = this.getJmxInfoModel(conf);
        this.getJmxClinetByModel(modelMap);
    }

    @Override
    protected void doStop() throws Exception {
        for (Map.Entry<AppMetricsKeyModel, JmxClient> entry : jmxClients.entrySet()) {
            JmxClient jmxClient = entry.getValue();
            if (jmxClient != null) {
                jmxClient.disconnect();
                if (jmxClient.getConnectionState() == ConnectionState.DISCONNECTED) {
                    LOGGER.info("Jmx connect to " + entry.getKey().getAppName() + " is closed !");
                }
            }
        }
    }

    @Override
    protected void doRestart() throws Exception {
        doStop();
        for (Map.Entry<AppMetricsKeyModel, JmxClient> entry : jmxClients.entrySet()) {
            JmxClient jmxClient = entry.getValue();
            jmxClient.connect();
            if (jmxClient.getConnectionState() == ConnectionState.CONNECTING) {
                LOGGER.info("Jmx is connecting to " + entry.getKey().getAppName());
            }
            if (jmxClient.getConnectionState() == ConnectionState.CONNECTED) {
                LOGGER.info("Jmx has connected to " + entry.getKey().getAppName());
            }
        }
    }

    /**
     * 
     * 功能描述: 监控自身GC信息
     * 
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public GCInfoModel getSelfGcInfo() {
        GCInfoModel gcInfoModel = null;
        if (jmxClients.isEmpty()) {
            LOGGER.warn("Jmx client is empty !");
            return null;
        }
        for (Map.Entry<AppMetricsKeyModel, JmxClient> entry : jmxClients.entrySet()) {
            AppMetricsKeyModel keyModel = entry.getKey();
            if ((keyModel.getIp().equals(MachineInfoUtil.getMachineIp()) || keyModel.getIp().equals(Constants.LOCAL_IP))
                    && !keyModel.isTarget()) {
                JmxClient jmxClient = entry.getValue();
                gcInfoModel = getGCinfo(keyModel, jmxClient);
            }
        }
        return gcInfoModel;
    }

    /**
     * 
     * 功能描述: 监控目标对象GC信息
     * 
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public GCInfoModel getTargetGCInfo() {
        GCInfoModel gcInfoModel = null;
        if (jmxClients.isEmpty()) {
            LOGGER.warn("Jmx client is empty !");
            return null;
        }
        for (Map.Entry<AppMetricsKeyModel, JmxClient> entry : jmxClients.entrySet()) {
            AppMetricsKeyModel keyModel = entry.getKey();
            if (keyModel.isTarget()) {
                JmxClient jmxClient = entry.getValue();
                gcInfoModel = getGCinfo(keyModel, jmxClient);
            }
        }
        return gcInfoModel;
    }
    /**
     * 
     * 功能描述: 监控扩展的JVM GC的信息
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public Map<String, GCInfoModel> getExtJmxGcInfo() {
        Map<String, GCInfoModel> mapResult = new HashMap<String, GCInfoModel>();
        if (jmxClients.isEmpty()) {
            LOGGER.warn("Jmx client is empty !");
            return null;
        }
        for (Map.Entry<AppMetricsKeyModel, JmxClient> entry : jmxClients.entrySet()) {
            AppMetricsKeyModel keyModel = entry.getKey();
            if (!keyModel.getIp().equals(MachineInfoUtil.getMachineIp())
                    && !keyModel.getIp().equals(Constants.LOCAL_IP)) {
                JmxClient jmxClient = entry.getValue();
                GCInfoModel gcInfoModel = getGCinfo(keyModel, jmxClient);
                mapResult.put(keyModel.toString(), gcInfoModel);
            }
        }
        return mapResult;
    }

    /**
     * 
     * 功能描述: 监控自身Heap信息
     * 
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public HeapInfoModel getSelfHeapInfo() {
        HeapInfoModel heapInfoModel = null;
        if (jmxClients.isEmpty()) {
            LOGGER.warn("Jmx client is empty !");
            return null;
        }
        for (Map.Entry<AppMetricsKeyModel, JmxClient> entry : jmxClients.entrySet()) {
            AppMetricsKeyModel keyModel = entry.getKey();
            if ((keyModel.getIp().equals(MachineInfoUtil.getMachineIp()) || keyModel.getIp().equals(Constants.LOCAL_IP))
                    && !keyModel.isTarget()) {
                JmxClient jmxClient = entry.getValue();
                heapInfoModel = getJvmMemInfo(keyModel, jmxClient);
            }
        }
        return heapInfoModel;
    }
    /**
     * 
     * 功能描述: 监控目标对象的Heap信息
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public HeapInfoModel getTargetHeapInfo(){
        HeapInfoModel heapInfoModel = null;
        if (jmxClients.isEmpty()) {
            LOGGER.warn("Jmx client is empty !");
            return null;
        }
        for (Map.Entry<AppMetricsKeyModel, JmxClient> entry : jmxClients.entrySet()) {
            AppMetricsKeyModel keyModel = entry.getKey();
            if (keyModel.isTarget()) {
                JmxClient jmxClient = entry.getValue();
                heapInfoModel = getJvmMemInfo(keyModel, jmxClient);
            }
        }
        return heapInfoModel;
    }
    /**
     * 
     * 功能描述: 监控扩展的JVM Heap的信息
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public Map<String,HeapInfoModel> getExtJmxHeapInfo(){
        Map<String, HeapInfoModel> mapResult = new HashMap<String, HeapInfoModel>();
        if (jmxClients.isEmpty()) {
            LOGGER.warn("Jmx client is empty !");
            return null;
        }
        for (Map.Entry<AppMetricsKeyModel, JmxClient> entry : jmxClients.entrySet()) {
            AppMetricsKeyModel keyModel = entry.getKey();
            if (!keyModel.getIp().equals(MachineInfoUtil.getMachineIp())
                    && !keyModel.getIp().equals(Constants.LOCAL_IP)) {
                JmxClient jmxClient = entry.getValue();
                HeapInfoModel heapInfoModel = getJvmMemInfo(keyModel, jmxClient);
                mapResult.put(keyModel.toString(), heapInfoModel);
            }
        }
        return mapResult;
    }
    /**
     * 
     * 功能描述: 通过JmxClient对象拿到GC的信息，遍历出结果，放入模型当中
     * 
     * @param keyModel
     * @param jmxClient
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private GCInfoModel getGCinfo(AppMetricsKeyModel keyModel, JmxClient jmxClient) {
        LOGGER.info("Starting get " + keyModel.getAppName() + " GC information !");
        GCInfoModel gcInfoModel = new GCInfoModel();
        try {
            Collection<GarbageCollectorMXBean> mxBeans = jmxClient.getGarbageCollectorMXBeans();
            Iterator<GarbageCollectorMXBean> iterator = mxBeans.iterator();
            boolean flag = true;
            while (iterator.hasNext()) {
                GarbageCollectorMXBean garbageCollectorMXBean = iterator.next();
                String gcName = garbageCollectorMXBean.getName();
                if (gcName.equals(GCConnector.PS_MARKSWEEP.getName())) {
                    gcInfoModel.setFullGCCount(garbageCollectorMXBean.getCollectionCount());
                    gcInfoModel.setFullgcTime(garbageCollectorMXBean.getCollectionTime());
                    flag = false;
                }
                if (gcName.equals(GCConnector.PS_SCAVENGE.getName())) {
                    gcInfoModel.setYoungGCConut(garbageCollectorMXBean.getCollectionCount());
                    gcInfoModel.setYoungTime(garbageCollectorMXBean.getCollectionTime());
                }
                if (flag) {
                    if (gcName.equals(GCConnector.PAR_NEW.getName())) {
                        gcInfoModel.setYoungGCConut(garbageCollectorMXBean.getCollectionCount());
                        gcInfoModel.setYoungTime(garbageCollectorMXBean.getCollectionTime());
                    }
                    if (gcName.equals(GCConnector.CMS.getName())) {
                        gcInfoModel.setFullGCCount(garbageCollectorMXBean.getCollectionCount());
                        gcInfoModel.setFullgcTime(garbageCollectorMXBean.getCollectionTime());
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Get GCInfo failed , Reconnecting !");
            jmxClient.disconnect();
            jmxClient.connect();
        } finally {
            jmxClient.flush();
        }
        return gcInfoModel;
    }

    /**
     * 
     * 功能描述: 通过JmxClient对象拿到Heap的信息，遍历出结果，放入模型当中
     * 
     * @param keyModel
     * @param jmxClient
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private HeapInfoModel getJvmMemInfo(AppMetricsKeyModel keyModel, JmxClient jmxClient) {
        LOGGER.info("Starting get " + keyModel.getAppName() + " Heap information !");
        HeapInfoModel heapInfoModel = new HeapInfoModel();
        try {
            MemoryMXBean memoryMXBean = jmxClient.getMemoryMXBean();
            MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
            heapInfoModel.setTotalMem(parseDoubleData(memoryUsage.getMax()));
            heapInfoModel.setCommitMem(parseDoubleData(memoryUsage.getCommitted()));
            heapInfoModel.setUsedMem(parseDoubleData(memoryUsage.getUsed()));
            LOGGER.info("当前堆的使用量----->: "+parseDoubleData(memoryUsage.getUsed())+"MB");
            Collection<MemoryPoolMXBean> memoryPoolMXBeans = jmxClient.getMemoryPoolMXBeans();
            Iterator<MemoryPoolMXBean> iterator = memoryPoolMXBeans.iterator();
            while (iterator.hasNext()) {
                MemoryPoolMXBean poolMXBean = iterator.next();
                String poolName = poolMXBean.getName();
                if (poolName.indexOf(HeapType.EDEN.getName()) != -1) {
                    // 返回 Java 虚拟机最近回收了此内存池中的不使用的对象之后的内存使用量getCollectionUsage()
                    MemoryUsage poolMemUsage = poolMXBean.getCollectionUsage();
                    heapInfoModel.setMaxEdenMem(parseDoubleData(poolMemUsage.getMax()));
                    heapInfoModel.setInitEdenMem(parseDoubleData(poolMemUsage.getInit()));
                    heapInfoModel.setEdenCommitMem(parseDoubleData(poolMemUsage.getCommitted()));
                    heapInfoModel.setEdenUsedMem(parseDoubleData(poolMemUsage.getUsed()));
                }
                if (poolName.indexOf(HeapType.SURVIROR.getName()) != -1) {
                    MemoryUsage poolMemUsage = poolMXBean.getCollectionUsage();
                    heapInfoModel.setMaxSurvirorMem(parseDoubleData(poolMemUsage.getMax()));
                    heapInfoModel.setInitSurvirorMem(parseDoubleData(poolMemUsage.getInit()));
                    heapInfoModel.setSurvirorCommitMem(parseDoubleData(poolMemUsage.getCommitted()));
                    heapInfoModel.setSurvirorUsedMem(parseDoubleData(poolMemUsage.getUsed()));
                }
                if (poolName.indexOf(HeapType.PERM.getName()) != -1) {
                    MemoryUsage poolMemUsage = poolMXBean.getCollectionUsage();
                    heapInfoModel.setMaxPermMem(parseDoubleData(poolMemUsage.getMax()));
                    heapInfoModel.setInitPermMem(parseDoubleData(poolMemUsage.getInit()));
                    heapInfoModel.setPermCommitMem(parseDoubleData(poolMemUsage.getCommitted()));
                    heapInfoModel.setPermUsedMem(parseDoubleData(poolMemUsage.getUsed()));
                }
                if (poolName.indexOf(HeapType.OLD.getName()) != -1) {
                    MemoryUsage poolMemUsage = poolMXBean.getCollectionUsage();
                    heapInfoModel.setMaxOldMem(parseDoubleData(poolMemUsage.getMax()));
                    heapInfoModel.setInitOldMem(parseDoubleData(poolMemUsage.getInit()));
                    heapInfoModel.setOldCommitMem(parseDoubleData(poolMemUsage.getCommitted()));
                    heapInfoModel.setOldUsedMem(parseDoubleData(poolMemUsage.getUsed()));
                }
            }
        } catch (IOException e) {
            LOGGER.error("Get HeapInfo failed , Reconnecting !",e);
        } finally {
            jmxClient.flush();
        }
        return heapInfoModel;
    }
    /**
     * 
     * 功能描述: 解析Heap数据大小的单位为MB，并保留两位小数
     *
     * @param l
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private double parseDoubleData(long l) {
        //可变长小数位，例如2.5就显示为2.5，不会为2.50
        DecimalFormat df = new DecimalFormat("#.##");
        double dataMB = l/1024/1024;
        String retData = df.format(dataMB);
        return Double.parseDouble(retData);
    }

    /**
     * 
     * 功能描述: 根据不同的配置来实例化JmxClient对象，放入map当中
     * 
     * @param modelMap
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private void getJmxClinetByModel(Map<String, GCInfoModel> modelMap) {
        if (modelMap.isEmpty()) {
            LOGGER.info("Please provided the correct JMX information !");
            return;
        }
        for (Map.Entry<String, GCInfoModel> jmxInfo : modelMap.entrySet()) {
            String appName = jmxInfo.getKey();
            GCInfoModel pModel = jmxInfo.getValue();
            int port = pModel.getPort() == null ? 0 : Integer.parseInt(pModel.getPort());
            if (pModel.getPidName() == null) {
                JmxClient jmxClient = null;
                try {
                    jmxClient = JmxClient.getJmxClient(pModel.getHost(), port, pModel.getUsername(),
                            pModel.getPassword());
                    jmxClient.connect();
                    if(jmxClient.getConnectionState() == ConnectionState.CONNECTED){
                        AppMetricsKeyModel keyModel = parseToModel(appName.replaceAll("\\.", "/") + "/"
                                + pModel.getHost() + "/" + pModel.isTarget());
                        LOGGER.info("Jmx has connected to " + keyModel.getAppName());
                        jmxClients.put(keyModel, jmxClient);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error(e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(),e);
                }
            } else {
                JmxClient jmxClient = null;
                String pidName = pModel.getPidName();
                int pid = getPidByName(pidName);
                try {
                    jmxClient = JmxClient.getJmxClient(pid);
                    jmxClient.connect();
                    if(jmxClient.getConnectionState() == ConnectionState.CONNECTED){
                        AppMetricsKeyModel keyModel = parseToModel(appName.replaceAll("\\.", "/") + "/"
                                + pModel.getHost() + "/" + pModel.isTarget());
                        LOGGER.info("Jmx has connected to " + keyModel.getAppName());
                        jmxClients.put(keyModel, jmxClient);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error(e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(),e);
                }
            }
        }
    }
    /**
     * 
     * 功能描述: 根据进程名称获取对应的进程PID
     *
     * @param pidName
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private int getPidByName(String pidName) {
        String linuxCommand = "jps | grep "+pidName+" | awk \'{print $1}\'";
        String [] cmdArray = new String[]{"/bin/bash", "-c", linuxCommand };
        List<String> commonResults = cmdOperateService.execute(cmdArray).getCommonResList();
        LOGGER.info("解析结果: "+commonResults);
        if(commonResults != null && commonResults.size() > 0){
            return Integer.parseInt(commonResults.get(0));
        }
        return 0;
    }

    private AppMetricsKeyModel parseToModel(String appName) {
        String[] temp = appName.trim().split("/");
        if (temp != null && temp.length == 4) {
            AppMetricsKeyModel target = new AppMetricsKeyModel(temp[0], temp[1], temp[2]);
            target.setTarget(Boolean.parseBoolean(temp[3]));
            return target;
        }
        LOGGER.warn("Can not parse String value to model , Please check the service_conf.properties key !");
        return null;
    }

    /**
     * 
     * 功能描述:读取属性文件当中JMX的相关信息，保存到map当中
     * 
     * @param conf
     * @return
     * @throws Exception
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private Map<String, GCInfoModel> getJmxInfoModel(Configuration conf) throws Exception {
        Iterator<Entry<String, String>> entry = conf.iterator();
        Map<String, GCInfoModel> modelMap = new HashMap<String, GCInfoModel>();
        while (entry.hasNext()) {
            Entry<String, String> result = entry.next();
            String key = result.getKey();
            if (key.startsWith(Constants.KEY_JMX_PREFIX)) {
                String appName = getAppName(key);
                String proName = getProName(key);
                String value = conf.get(key);
                if (!modelMap.containsKey(appName)) {
                    GCInfoModel pModel = new GCInfoModel();
                    if (!StringHelper.isBlank(value)) {
                        BeanUtils.setProperty(pModel, proName, value);
                    }
                    modelMap.put(appName, pModel);
                } else {
                    GCInfoModel pModel = modelMap.get(appName);
                    if (!StringHelper.isBlank(value)) {
                        BeanUtils.setProperty(pModel, proName, value);
                    }
                }
            }
        }
        return modelMap;
    }

    private String getAppName(String key) {
        return key.substring(key.indexOf(".", 1) + 1, key.lastIndexOf("."));
    }

    private String getProName(String key) {
        return key.substring(key.lastIndexOf(".") + 1);
    }
}
