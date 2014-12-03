package com.suning.app.spy.core.service.impl;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.suning.app.spy.core.AbstractLifeCycle;
import com.suning.app.spy.core.Configuration;
import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.service.Service;
import com.suning.app.spy.core.service.ServiceControl;
import com.suning.app.spy.core.utils.JmxUtil;
import com.suning.app.spy.core.utils.MachineInfoUtil;
import com.suning.app.spy.core.utils.StringHelper;
import com.suning.shared.spy.model.AppMetricsKeyModel;
import com.suning.shared.spy.model.GCInfoModel;
import com.suning.shared.spy.model.GCInfoModel.GCConnector;
import com.suning.shared.spy.model.HeapInfoModel;
import com.suning.shared.spy.model.HeapInfoModel.JvmType;

public class JmxOperateServiceTwo extends AbstractLifeCycle implements Service {
    
    private static final Logger LOGGER = Logger.getLogger(JmxOperateService.class);
    
    private Map<AppMetricsKeyModel,JMXConnector> conns = new HashMap<AppMetricsKeyModel, JMXConnector>();
    private CmdOperateService cmdOperateService = ServiceControl.getInstance().getBean(CmdOperateService.class);
    private JMXServiceURL serviceURL;
    private Configuration conf;
    
    @Override
    protected void doStart(Configuration conf) throws Exception {
        this.conf = conf;
        Map<String,GCInfoModel> modelMap = this.getJmxInfoModel(conf);
        this.getJmxConnectorByModel(modelMap);
    }

    @Override
    protected void doStop() throws Exception {
        for(Map.Entry<AppMetricsKeyModel, JMXConnector> entry : conns.entrySet()){
            JMXConnector connector = entry.getValue();
            if(connector != null){
                connector.close();
                LOGGER.info("Jmx connect to "+entry.getKey()+" is closed !");
            }
        }
    }

    @Override
    protected void doRestart() throws Exception {
        doStop();
        doStart(this.conf);
    }
    /**
     * 
     * 功能描述: 获取各个JVM进程的GC信息
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public Map<String,GCInfoModel> getExtJmxGcInfo(){
        Map<String,GCInfoModel> mapResult = new HashMap<String, GCInfoModel>();
        if(!conns.isEmpty()){
            for(Map.Entry<AppMetricsKeyModel, JMXConnector> entry : conns.entrySet()){
                AppMetricsKeyModel key = entry.getKey();
                if(!key.isTarget()){
                    JMXConnector con = entry.getValue();
                    MBeanServerConnection mbs = null;
                    try {
                        mbs = con.getMBeanServerConnection();
                    } catch (IOException e) {
                        LOGGER.info(e.getCause());
                    }
                    GCInfoModel gcInfoModel = this.getGCinfo(key, mbs);
                    mapResult.put(key.toString(), gcInfoModel);
                }
            }
        }
        return mapResult;
    }
    /**
     * 
     * 功能描述: 获取各个JVM进程的内存分配信息
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public Map<String,HeapInfoModel> getExtJmxHeapInfo(){
        Map<String,HeapInfoModel> mapResult = new HashMap<String, HeapInfoModel>();
        if(!conns.isEmpty()){
            for(Map.Entry<AppMetricsKeyModel, JMXConnector> entry: conns.entrySet()){
                AppMetricsKeyModel key = entry.getKey();
                if(!key.isTarget()){
                    JMXConnector con = entry.getValue();
                    MBeanServerConnection mbs = null;
                    try {
                        mbs = con.getMBeanServerConnection();
                    } catch (IOException e) {
                        LOGGER.info(e.getCause());
                    }
                    HeapInfoModel heapInfoModel = this.getJvmMemInfo(key, mbs);
                    mapResult.put(key.toString(), heapInfoModel);
                }
            }
        }
        return mapResult;
    }
    /**
     * 
     * 功能描述: 获取目标对象的heap信息
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public HeapInfoModel getTargetHeapInfo(){
        HeapInfoModel heapInfoModel = null;
        for(Map.Entry<AppMetricsKeyModel, JMXConnector> entry : conns.entrySet()){
            AppMetricsKeyModel keyModel = entry.getKey();
            if(keyModel.isTarget()){
                JMXConnector con = entry.getValue();
                MBeanServerConnection mbs = null;
                try {
                    mbs = con.getMBeanServerConnection();
                } catch (IOException e) {
                    LOGGER.error(e.getCause());
                }
                heapInfoModel = getJvmMemInfo(keyModel, mbs);
            }
        }
        return heapInfoModel;
    }
    /**
     * 
     * 功能描述: 获取目标进程的GC信息
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public GCInfoModel getTargetGCInfo(){
        GCInfoModel gcInfoModel = null;
        for(Map.Entry<AppMetricsKeyModel,JMXConnector> entry : conns.entrySet()){
            AppMetricsKeyModel keyModel = entry.getKey();
            if(keyModel.isTarget()){
                JMXConnector con = entry.getValue();
                MBeanServerConnection mbs = null;
                try {
                    mbs = con.getMBeanServerConnection();
                } catch (IOException e) {
                    LOGGER.error(e.getCause());
                }
                gcInfoModel = getGCinfo(keyModel, mbs);
            }
        }
        return gcInfoModel;
    }
    /**
     * 
     * 功能描述:获取自身JVM进程的GC信息
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public GCInfoModel getSelfGcInfo(){
        MBeanServerConnection mbs = ManagementFactory.getPlatformMBeanServer();
        GCInfoModel gcInfoModel = getGCinfo(null, mbs);
        return gcInfoModel;
    }
    /**
     * 
     * 功能描述:获取自身JVM的内存信息
     *
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public HeapInfoModel getSelfHeapInfo(){
        MBeanServerConnection mbs = ManagementFactory.getPlatformMBeanServer();
        HeapInfoModel heapInfoModel = getJvmMemInfo(null, mbs);
        return heapInfoModel;
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
    private Map<String,GCInfoModel> getJmxInfoModel(Configuration conf) throws Exception{
        Iterator<Entry<String, String>> entry = conf.iterator();
        Map<String, GCInfoModel> modelMap = new HashMap<String, GCInfoModel>();
        while(entry.hasNext()){
            Entry<String,String> result = entry.next();
            String key = result.getKey();
            if(key.startsWith(Constants.KEY_JMX_PREFIX)){
                String appName = getAppName(key);
                String proName = getProName(key);
                String value = conf.get(key);
                if(!modelMap.containsKey(appName)){
                    GCInfoModel pModel = new GCInfoModel();
                    if(!StringHelper.isBlank(value)){
                        BeanUtils.setProperty(pModel, proName, value);
                    }
                    modelMap.put(appName, pModel);
                }else{
                    GCInfoModel pModel = modelMap.get(appName);
                    if(!StringHelper.isBlank(value)){
                        BeanUtils.setProperty(pModel, proName, value);
                    }
                }
            }
        }
        return modelMap;
    }
    /**
     * 
     * 功能描述: 根据JMX连接对象的不同创建出不同的connector，放入map当中
     *
     * @param modelMap
     * @throws IOException
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private void getJmxConnectorByModel(Map<String,GCInfoModel> modelMap) throws IOException{
        /*用户名、密码  
        Map<String, String[]> map = new HashMap<String, String[]>();  
        map.put("jmx.remote.credentials", new String[] { "monitorRole",  
           "QED" });
        */
        if(modelMap.isEmpty()){
            LOGGER.info("Please provided the correct JMX information !");
            return;
        }
        for(Map.Entry<String, GCInfoModel> jmxInfo : modelMap.entrySet()){
            String appName = jmxInfo.getKey();
            GCInfoModel pModel = jmxInfo.getValue();
            if(!(pModel.getHost().equals(MachineInfoUtil.getMachineIp())) && !Constants.LOCAL_IP.equals(pModel.getHost())){
                if(pModel.getUsername() != null && pModel.getPassword() != null){
                    Map<String, String[]> map = new HashMap<String, String[]>();
                    map.put("jmx.remote.credentials", new String[] {pModel.getUsername(),pModel.getPassword()});
                    serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+pModel.getHost()+":"+pModel.getPort()+"/jmxrmi");
                    JMXConnector con = JMXConnectorFactory.connect(serviceURL,map);
                    conns.put(parseToModel(appName.replaceAll("\\.", "/")+"/"+pModel.getHost()+"/"+pModel.isTarget()), con);
                }else{
                    serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+pModel.getHost()+":"+pModel.getPort()+"/jmxrmi");
                    JMXConnector con = JMXConnectorFactory.connect(serviceURL);
                    conns.put(parseToModel(appName.replaceAll("\\.", "/")+"/"+pModel.getHost()+"/"+pModel.isTarget()), con);
                }
            }else{
                String pidName = pModel.getPidName();
                if(pidName!= null && pidName.length() > 0){
                    int pid = getPidByName(pidName);
                    String jmxUrl = JmxUtil.findJMXUrlByProcessId(pid);
                    serviceURL = new JMXServiceURL(jmxUrl);
                    JMXConnector con = JMXConnectorFactory.connect(serviceURL);
                    conns.put(parseToModel(appName.replaceAll("\\.", "/")+"/"+pModel.getHost()+"/"+pModel.isTarget()), con);
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
        if(commonResults != null && commonResults.size() > 0){
            return Integer.parseInt(commonResults.get(0));
        }
        return 0;
    }
    /**
     * 
     * 功能描述: 解析配置文件的key为对应的AppMetricsKeyModel模型
     * 〈功能详细描述〉
     *
     * @param appName
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private AppMetricsKeyModel parseToModel(String appName) {
        String [] temp = appName.trim().split("/");
        if(temp != null && temp.length ==4){
            AppMetricsKeyModel target = new AppMetricsKeyModel(temp[0],temp[1], temp[2]);
            target.setTarget(Boolean.parseBoolean(temp[3]));
            return target;
        }
        LOGGER.warn("Can not parse String value to model , Please check the service_conf.properties key !");
        return null;
    }

    private String getAppName(String key){
        return key.substring(key.indexOf(".", 1) + 1, key.lastIndexOf("."));
    }
    private String getProName(String key) {
        return key.substring(key.lastIndexOf(".") + 1);
    }
    /**
     * 
     * 功能描述: 根据垃圾回收方式的不同取得对应的gc时间和次数
     * Oracle (Sun) HotSpot jdk6+  
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private GCInfoModel getGCinfo(AppMetricsKeyModel keyModel , MBeanServerConnection mbs){
        LOGGER.info("Starting get "+keyModel+" GC information !");
        GCInfoModel gcInfoModel = new GCInfoModel();
        boolean flag = getParrelGcInfo(gcInfoModel, mbs);
        if(flag){
            getCmsGcInfo(gcInfoModel, mbs);
        }
        return gcInfoModel;
    }
    /**
     * 
     * 功能描述: 采集CMS算法垃圾回收的GC信息
     *
     * @param gcInfoModel
     * @param mbs
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private void getCmsGcInfo(GCInfoModel gcInfoModel,MBeanServerConnection mbs){
        try {
            GarbageCollectorMXBean garbageCollectorMXBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE+",name="+GCConnector.PAR_NEW.getName(), GarbageCollectorMXBean.class);
            LOGGER.info("Young GC Name : "+garbageCollectorMXBean.getName());
            //如果是并发GC(CMS)，计算yong gc的次数和总时间
            gcInfoModel.setYoungGCConut(garbageCollectorMXBean.getCollectionCount());
            gcInfoModel.setYoungTime(garbageCollectorMXBean.getCollectionTime());
        } catch(IllegalArgumentException e){
            LOGGER.warn("java.lang:type=GarbageCollector,name="+GCConnector.PAR_NEW.getName()+"instanceNotFound");
        } catch (IOException e) {
            LOGGER.warn("Obtain MXBean failed !");
        }
        try {
            GarbageCollectorMXBean garbageCollectorMXBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE+",name="+GCConnector.CMS.getName(), GarbageCollectorMXBean.class);
            LOGGER.info("Full GC Name : "+garbageCollectorMXBean.getName());
            //如果是并发GC(CMS)，计算full gc的次数和总时间
            gcInfoModel.setFullGCCount(garbageCollectorMXBean.getCollectionCount());
            gcInfoModel.setFullgcTime(garbageCollectorMXBean.getCollectionTime());
        } catch(IllegalArgumentException e){
            LOGGER.warn("java.lang:type=GarbageCollector,name="+GCConnector.CMS.getName()+"instanceNotFound");
        } catch (IOException e) {
            LOGGER.warn("Obtain MXBean failed !");
        }
    }
    /**
     * 
     * 功能描述: 采集并行垃圾回收算法Parrel的GC信息
     *
     * @param gcInfoModel
     * @param mbs
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private boolean getParrelGcInfo(GCInfoModel gcInfoModel,MBeanServerConnection mbs){
        boolean flag = true;
        //获取远程GarbageCollectorMXBean
        try {
            GarbageCollectorMXBean garbageCollectorMXBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE+",name="+GCConnector.PS_SCAVENGE.getName(), GarbageCollectorMXBean.class);;
            LOGGER.info("Young GC Name : "+garbageCollectorMXBean.getName());
            //如果是并行GC(Parallel)，计算yong gc的次数和总时间
            gcInfoModel.setYoungGCConut(garbageCollectorMXBean.getCollectionCount());
            gcInfoModel.setYoungTime(garbageCollectorMXBean.getCollectionTime());
            flag = false;
        } catch(IllegalArgumentException e){
            LOGGER.warn("java.lang:type=GarbageCollector,name="+GCConnector.PS_SCAVENGE.getName()+"instanceNotFound");
        } catch (IOException e) {
            LOGGER.warn("Obtain MXBean failed !");
        }
        try {
            GarbageCollectorMXBean garbageCollectorMXBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE+",name="+GCConnector.PS_MARKSWEEP.getName(), GarbageCollectorMXBean.class);
            LOGGER.info("Full GC Name : "+garbageCollectorMXBean.getName());
            //如果是并行GC，计算full gc的次数和总时间
            gcInfoModel.setFullGCCount(garbageCollectorMXBean.getCollectionCount());
            gcInfoModel.setFullgcTime(garbageCollectorMXBean.getCollectionTime());
        } catch(IllegalArgumentException e){
            LOGGER.warn("java.lang:type=GarbageCollector,name="+GCConnector.PS_MARKSWEEP.getName()+"instanceNotFound");
        } catch (IOException e) {
            LOGGER.warn("Obtain MXBean failed !");
        }
        return flag;
    }
    /**
     * 
     * 功能描述: <br>
     * 针对分代式垃圾回收，根据不同的GC方式获取各个代区的内存分配和使用情况
     * Oracle (Sun) HotSpot jdk6+
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private HeapInfoModel getJvmMemInfo(AppMetricsKeyModel keyModel , MBeanServerConnection mbs){
        LOGGER.info("Starting get "+keyModel+" heap information !");
        HeapInfoModel jvmMemoryInfoModel = new HeapInfoModel();
        try {
            //采集总的内存信息
            MemoryMXBean memBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
            MemoryUsage memoryUsage = memBean.getHeapMemoryUsage();
            jvmMemoryInfoModel.setTotalMem(memoryUsage.getMax());
            jvmMemoryInfoModel.setCommitMem(memoryUsage.getCommitted());
            jvmMemoryInfoModel.setUsedMem(memoryUsage.getUsed());
        } catch (IOException e) {
            LOGGER.error("Obtain MemoryMXBean failed !");
        }
        boolean flag = getHeapInfoByParrel(jvmMemoryInfoModel, mbs);
        if(flag){
            getHeapInfoByCMS(jvmMemoryInfoModel, mbs);
        }
        return jvmMemoryInfoModel;
    }
    /**
     * 
     * 功能描述: 采集并发方式CMS模式下各个代区的内存分配情况
     *
     * @param jvmMemoryInfoModel
     * @param mbs
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private void getHeapInfoByCMS(HeapInfoModel jvmMemoryInfoModel,MBeanServerConnection mbs){
        try {
            MemoryPoolMXBean memPoolBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PAR_EDEN_SPACE.getName(), MemoryPoolMXBean.class);
            MemoryUsage memoryUsage = memPoolBean.getCollectionUsage();
            jvmMemoryInfoModel.setMaxEdenMem(memoryUsage.getMax());
            jvmMemoryInfoModel.setInitEdenMem(memoryUsage.getInit());
            jvmMemoryInfoModel.setEdenCommitMem(memoryUsage.getCommitted());
            jvmMemoryInfoModel.setEdenUsedMem(memoryUsage.getUsed());
            }catch(IllegalArgumentException e){
                LOGGER.warn(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PAR_EDEN_SPACE.getName()+"instanceNotFound");
            }
            catch (IOException e) {
                LOGGER.error("Obtain MemoryPoolMXBean failed !");
            }
        try {
            MemoryPoolMXBean memPoolBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PAR_SURVIROR_SPACE.getName(), MemoryPoolMXBean.class);
            MemoryUsage memoryUsage = memPoolBean.getCollectionUsage();
            jvmMemoryInfoModel.setMaxSurvirorMem(memoryUsage.getMax());
            jvmMemoryInfoModel.setInitSurvirorMem(memoryUsage.getInit());
            jvmMemoryInfoModel.setSurvirorCommitMem(memoryUsage.getCommitted());
            jvmMemoryInfoModel.setSurvirorUsedMem(memoryUsage.getUsed());
            }catch(IllegalArgumentException e){
                LOGGER.warn(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PAR_SURVIROR_SPACE.getName()+"instanceNotFound");
            }
            catch (IOException e) {
                LOGGER.error("Obtain MemoryPoolMXBean failed !");
            }
        try {
            MemoryPoolMXBean memPoolBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.CMS_OLD_GEN.getName(), MemoryPoolMXBean.class);
            MemoryUsage memoryUsage = memPoolBean.getCollectionUsage();
            jvmMemoryInfoModel.setMaxOldMem(memoryUsage.getMax());
            jvmMemoryInfoModel.setInitOldMem(memoryUsage.getInit());
            jvmMemoryInfoModel.setOldCommitMem(memoryUsage.getCommitted());
            jvmMemoryInfoModel.setOldUsedMem(memoryUsage.getUsed());
            }catch(IllegalArgumentException e){
                LOGGER.warn(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.CMS_OLD_GEN.getName()+"instanceNotFound");
            }
            catch (IOException e) {
                LOGGER.error("Obtain MemoryPoolMXBean failed !");
            }
        try {
            MemoryPoolMXBean memPoolBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.CMS_PERM_GEN.getName(), MemoryPoolMXBean.class);
            MemoryUsage memoryUsage = memPoolBean.getCollectionUsage();
            jvmMemoryInfoModel.setMaxPermMem(memoryUsage.getMax());
            jvmMemoryInfoModel.setInitPermMem(memoryUsage.getInit());
            jvmMemoryInfoModel.setPermCommitMem(memoryUsage.getCommitted());
            jvmMemoryInfoModel.setPermUsedMem(memoryUsage.getUsed());
            }catch(IllegalArgumentException e){
                LOGGER.warn(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.CMS_PERM_GEN.getName()+"instanceNotFound");
            }
            catch (IOException e) {
                LOGGER.error("Obtain MemoryPoolMXBean failed !");
            }
    }
    /**
     * 
     * 功能描述: 采集Parrel并行GC模式下，各个代区的内存分配情况
     *
     * @param jvmMemoryInfoModel
     * @param mbs
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private boolean getHeapInfoByParrel(HeapInfoModel jvmMemoryInfoModel,MBeanServerConnection mbs){
        boolean flag = true;
        try {
            MemoryPoolMXBean memPoolBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PS_EDEN_SPACE.getName(), MemoryPoolMXBean.class);
            MemoryUsage memoryUsage = memPoolBean.getCollectionUsage();
            jvmMemoryInfoModel.setMaxEdenMem(memoryUsage.getMax());
            jvmMemoryInfoModel.setInitEdenMem(memoryUsage.getInit());
            jvmMemoryInfoModel.setEdenCommitMem(memoryUsage.getCommitted());
            jvmMemoryInfoModel.setEdenUsedMem(memoryUsage.getUsed());
            flag = false;
            }catch(IllegalArgumentException e){
                LOGGER.warn(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PS_EDEN_SPACE.getName()+" instanceNotFound");
            }
            catch (IOException e) {
                LOGGER.error("Obtain MemoryPoolMXBean failed !");
            }
        try {
            MemoryPoolMXBean memPoolBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PS_SURVIROR_SPACE.getName(), MemoryPoolMXBean.class);
            MemoryUsage memoryUsage = memPoolBean.getCollectionUsage();
            jvmMemoryInfoModel.setMaxSurvirorMem(memoryUsage.getMax());
            jvmMemoryInfoModel.setInitSurvirorMem(memoryUsage.getInit());
            jvmMemoryInfoModel.setSurvirorCommitMem(memoryUsage.getCommitted());
            jvmMemoryInfoModel.setSurvirorUsedMem(memoryUsage.getUsed());
            }catch(IllegalArgumentException e){
                LOGGER.warn(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PS_SURVIROR_SPACE.getName()+" instanceNotFound");
            }
            catch (IOException e) {
                LOGGER.error("Obtain MemoryPoolMXBean failed !");
            }
        try {
            MemoryPoolMXBean memPoolBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PS_OLD_GEN.getName(), MemoryPoolMXBean.class);
            MemoryUsage memoryUsage = memPoolBean.getCollectionUsage();
            jvmMemoryInfoModel.setMaxOldMem(memoryUsage.getMax());
            jvmMemoryInfoModel.setInitOldMem(memoryUsage.getInit());
            jvmMemoryInfoModel.setOldCommitMem(memoryUsage.getCommitted());
            jvmMemoryInfoModel.setOldUsedMem(memoryUsage.getUsed());
            }catch(IllegalArgumentException e){
                LOGGER.warn(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PS_OLD_GEN.getName()+" instanceNotFound");
            }
            catch (IOException e) {
                LOGGER.error("Obtain MemoryPoolMXBean failed !");
            }
        try {
            MemoryPoolMXBean memPoolBean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PS_PERM_GEN.getName(), MemoryPoolMXBean.class);
            MemoryUsage memoryUsage = memPoolBean.getCollectionUsage();
            jvmMemoryInfoModel.setMaxPermMem(memoryUsage.getMax());
            jvmMemoryInfoModel.setInitPermMem(memoryUsage.getInit());
            jvmMemoryInfoModel.setPermCommitMem(memoryUsage.getCommitted());
            jvmMemoryInfoModel.setPermUsedMem(memoryUsage.getUsed());
            }catch(IllegalArgumentException e){
                LOGGER.warn(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE+",name="+JvmType.PS_PERM_GEN.getName()+" instanceNotFound");
            }
            catch (IOException e) {
                LOGGER.error("Obtain MemoryPoolMXBean failed !");
            }
        return flag;
    }
    public static void main(String[] args) {
        /*JmxOperateService jmxOperateService = new JmxOperateService();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        GCInfoModel gcInfoModel = jmxOperateService.getGCinfo(mbs);
        System.out.println("gc count = "+gcInfoModel.getYoungGCConut());
        JvmMemoryInfoModel infoModel = jmxOperateService.getJvmMemInfo(mbs);
        System.out.println(infoModel.getTotalMem());
        System.out.println(infoModel.getMaxEdenMem());
        System.out.println(infoModel.getMaxSurvirorMem());
        System.out.println(infoModel.getMaxOldMem());
        System.out.println(infoModel.getMaxPermMem());*/
    }
}