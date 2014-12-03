package com.suning.app.spy.core.constants;

/**
 * 类Constants.java的实现描述：RT
 * @author karry 2014-10-24 下午12:45:28
 */
public abstract class Constants {
	
	public static final String COMMON_CONF_FILE = "/spy_conf.properties";
	public static final String SERVICE_CONF_FILE = "/service_conf.properties";
	public static final String COMMAND_CONF_FILE = "/command_conf.properties";
	

	/***********Key************/
	
	//common
	public static final String KEY_CHAR_SET = "charSet";
	public static final String KEY_COMMAND_EXEC_THREAD_COUNT = "commandExecThreadCount";
	public static final String KEY_COMMAND_EXEC_TIME_OUT = "commandExecTimeOut";
	/**
	 * 重试的间隔毫秒数
	 */
	public static final String KEY_RETRY_INTERVAL = "retryInterval";
	
	/**
	 * 每次重试的次数
	 */
	public static final String KEY_RETRY_COUNT_LIMIT = "retryCountLimit";
	public static final String KEY_SEND_INTERVAL = "sendInterval";
	public static final String VALUE_CONTENT_SEP = ",";
	
	
	//linux command
	public static final String KEY_SPLIT = ".";
	public static final String METRICS_MEM = "mem";
    public static final String METRICS_CPU = "cpu";
    public static final String METRICS_DISK = "disk";
    public static final String METRICS_NET = "net";
    public static final String METRICS_GC = "gc";
    public static final String METRICS_HEAP = "heap";
    
    public static final String KEY_COMMAND_ENABLE = "enable";
    public static final String KEY_TIMER_DELAY_TIME = "delayTime";
    public static final String KEY_COMMAND_CONTENT = "content";
	
	//metrics target
	public static final String KEY_APP_NAME = "appName";	
	public static final String KEY_CLUSTER_NAME = "clusterName";	
	
	//zk
	public static final String KEY_ZK_ADDRESS = "zkAddress";
	
	
	//JMX
	public static final String KEY_JMX_PREFIX="jmx";
    public static final String SELF_GC_INFO = "self";
    public static final String SELF_HEAP_INFO = "self";
    public static final String LOCAL_IP = "127.0.0.1";
    
}
