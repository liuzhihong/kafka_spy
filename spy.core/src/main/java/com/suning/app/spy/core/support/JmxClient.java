package com.suning.app.spy.core.support;

import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.utils.JmxUtil;
import com.suning.app.spy.core.utils.MachineInfoUtil;

/**
 * 类JmxClient.java的实现描述：Cached&reConnected的Jmx客户端
 * 
 * @author karry 2014-11-13 上午10:24:03
 */
public class JmxClient {

	final private String connectionName;

	private ConnectionState connectionState = ConnectionState.DISCONNECTED;

	private static Map<String, JmxClient> cache = Collections.synchronizedMap(new HashMap<String, JmxClient>());

	private String hostName = null;

	private int port = 0;

	private String userName = null;

	private String password = null;

	private boolean hasPlatformMXBeans = false;

	private boolean hasCompilationMXBean = false;

	private boolean supportsLockUsage = false;

	private String advancedUrl = null;

	private JMXServiceURL jmxUrl = null;

	private MBeanServerConnection mbsc = null;

	private AdvancedMBeanServerConnection server = null;

	private JMXConnector jmxc = null;

	private int lvmPid = 0;

	private ClassLoadingMXBean classLoadingMBean = null;

	private CompilationMXBean compilationMBean = null;

	private MemoryMXBean memoryMBean = null;

	private OperatingSystemMXBean operatingSystemMBean = null;

	private RuntimeMXBean runtimeMBean = null;

	private ThreadMXBean threadMBean = null;

	private List<GarbageCollectorMXBean> garbageCollectorMBeans = null;

	private List<MemoryPoolMXBean> memoryPoolMXBeans = null;

	private static final long ALIVE_SCAN_INTERVAL = 600000;

	private static final long ALIVE_SCAN_DELAY = 60000;

	static {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				if (cache.isEmpty()) {
					return;
				}
				for (Entry<String, JmxClient> entry : cache.entrySet()) {
					JmxClient client = entry.getValue();
					if (!client.isConnected()) {
						try {
							// 下面这两段有并发问题，不过无所谓，并发和重试在这里可以不考虑
							client.disconnect();
							client.connect();

						} catch (Exception e) {

						}
					}
				}
			}

		}, ALIVE_SCAN_DELAY, ALIVE_SCAN_INTERVAL);
	}

	private JmxClient(String hostName, int port, String userName, String password) throws IOException {
		this.connectionName = getConnectionName(hostName, port, userName);
		if ((hostName.equals(MachineInfoUtil.getMachineIp()) || hostName.equals(Constants.LOCAL_IP)) && port == 0) {
			// Monitor self
			this.hostName = hostName;
			this.port = port;
		} else {
			// Create an RMI connector client and connect it to
			// the RMI connector server
			final String urlPath = "/jndi/rmi://" + hostName + ":" + port + "/jmxrmi";
			JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);
			setParameters(url, userName, password);
		}
	}

	private JmxClient(String url, String userName, String password) throws IOException {
		this.advancedUrl = url;
		this.connectionName = getConnectionName(url, userName);
		setParameters(new JMXServiceURL(url), userName, password);
	}

	private JmxClient(int lvmPid) throws IOException {
		this.lvmPid = lvmPid;
		this.connectionName = getConnectionName(lvmPid);
		this.jmxUrl = new JMXServiceURL(JmxUtil.findJMXUrlByProcessId(lvmPid));
	}

	private void setParameters(JMXServiceURL url, String userName, String password) {
		this.jmxUrl = url;
		this.hostName = jmxUrl.getHost();
		this.port = jmxUrl.getPort();
		this.userName = userName;
		this.password = password;
	}

	public static String getConnectionName(String url, String userName) {
		if (userName != null && userName.length() > 0) {
			return userName + "@" + url;
		} else {
			return url;
		}
	}

	public static String getConnectionName(String hostName, int port, String userName) {
		String name = hostName + ":" + port;
		if (userName != null && userName.length() > 0) {
			return userName + "@" + name;
		} else {
			return name;
		}
	}

	public static String getConnectionName(long lvmPid) {
		return Long.toString(lvmPid);
	}

	public ConnectionState getConnectionState() {
		return this.connectionState;
	}

	public void flush() {
		if (server != null) {
			server.flush();
		}
	}

	private boolean isDead() {
		if (server != null) {
			return server.isDead();
		}
		return false;
	}

	private void markDead() {
		if (server != null) {
			server.markDead();
		}
	}

	boolean isConnected() {
		return !isDead() && connectionState.equals(ConnectionState.CONNECTED);
	}

	public void connect() {
		connectionState = ConnectionState.CONNECTING;
		try {
			tryConnect();
			connectionState = ConnectionState.CONNECTED;
		} catch (Exception e) {
			disconnect();
		}
	}

	private void tryConnect() throws IOException {
		if (jmxUrl == null && (hostName.equals(MachineInfoUtil.getMachineIp()) || hostName.equals(Constants.LOCAL_IP)) && port == 0) {
			// Monitor self
			this.jmxc = null;
			this.mbsc = ManagementFactory.getPlatformMBeanServer();
			this.server = Advanced.newAdvanced(mbsc);
		} else {
			if (userName == null && password == null) {
				this.jmxc = JMXConnectorFactory.connect(jmxUrl);
			} else {
				Map<String, String[]> env = new HashMap<String, String[]>();
				env.put(JMXConnector.CREDENTIALS, new String[] { userName, password });
				this.jmxc = JMXConnectorFactory.connect(jmxUrl, env);
			}
			this.mbsc = jmxc.getMBeanServerConnection();
			this.server = Advanced.newAdvanced(mbsc);
		}
		this.server.markAlive();

		try {
			ObjectName on = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
			this.hasPlatformMXBeans = server.isRegistered(on);
			// check if it has 6.0 new APIs
			if (this.hasPlatformMXBeans) {
				MBeanOperationInfo[] mopis = server.getMBeanInfo(on).getOperations();
				// look for findDeadlockedThreads operations;
				for (MBeanOperationInfo op : mopis) {
					if (op.getName().equals("findDeadlockedThreads")) {
						this.supportsLockUsage = true;
						break;
					}
				}

				on = new ObjectName(ManagementFactory.COMPILATION_MXBEAN_NAME);
				this.hasCompilationMXBean = server.isRegistered(on);
			}
		} catch (MalformedObjectNameException e) {
			// should not reach here
			throw new InternalError(e.getMessage());
		} catch (IntrospectionException e) {
			InternalError ie = new InternalError(e.getMessage());
			ie.initCause(e);
			throw ie;
		} catch (InstanceNotFoundException e) {
			InternalError ie = new InternalError(e.getMessage());
			ie.initCause(e);
			throw ie;
		} catch (ReflectionException e) {
			InternalError ie = new InternalError(e.getMessage());
			ie.initCause(e);
			throw ie;
		}

		if (hasPlatformMXBeans) {
			// WORKAROUND for bug 5056632
			// Check if the access role is correct by getting a RuntimeMXBean
			getRuntimeMXBean();
		}
	}

	public static JmxClient getJmxClient(int lvmPid) throws IOException {
		final String key = getCacheKey(lvmPid);
		JmxClient jmxClient = cache.get(key);
		if (jmxClient == null) {
			jmxClient = new JmxClient(lvmPid);
			cache.put(key, jmxClient);
		}
		return jmxClient;
	}

	private static String getCacheKey(int lvmPid) {
		return Integer.toString(lvmPid);
	}

	public static JmxClient getJmxClient(String url, String userName, String password)
			throws IOException {
		final String key = getCacheKey(url, userName, password);
		JmxClient jmxClient = cache.get(key);
		if (jmxClient == null) {
			jmxClient = new JmxClient(url, userName, password);
			cache.put(key, jmxClient);
		}
		return jmxClient;
	}

	private static String getCacheKey(String url, String userName, String password) {
		return (url == null ? "" : url) + ":" + (userName == null ? "" : userName) + ":" + (password == null ? "" : password);
	}

	public static JmxClient getJmxClient(String hostName, int port, String userName, String password)
			throws IOException {
		final String key = getCacheKey(hostName, port, userName, password);
		JmxClient jmxClient = cache.get(key);
		if (jmxClient == null) {
			jmxClient = new JmxClient(hostName, port, userName, password);
			cache.put(key, jmxClient);
		}
		return jmxClient;
	}

	private static String getCacheKey(String hostName, int port, String userName, String password) {
		return (hostName == null ? "" : hostName) + ":" + port + ":" + (userName == null ? "" : userName) + ":" + (password == null ? "" : password);
	}

	public String connectionName() {
		return connectionName;
	}

	public MBeanServerConnection getMBeanServerConnection() {
		return mbsc;
	}

	public AdvancedMBeanServerConnection getAdvancedMBeanServerConnection() {
		return server;
	}

	public String getUrl() {
		return advancedUrl;
	}

	public String getHostName() {
		return hostName;
	}

	public int getPort() {
		return port;
	}

	public int getVmid() {
		return lvmPid;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public void disconnect() {
		// Close MBeanServer connection
		if (jmxc != null) {
			try {
				jmxc.close();
			} catch (IOException e) {
				// Ignore ???
			}
		}
		// Reset platform MBean references
		classLoadingMBean = null;
		compilationMBean = null;
		memoryMBean = null;
		operatingSystemMBean = null;
		runtimeMBean = null;
		threadMBean = null;
		garbageCollectorMBeans = null;
		markDead();
		connectionState = ConnectionState.DISCONNECTED;

	}

	public String[] getDomains() throws IOException {
		return server.getDomains();
	}

	@SuppressWarnings("rawtypes")
	public Map<ObjectName, MBeanInfo> getMBeans(String domain) throws IOException {

		ObjectName name = null;
		if (domain != null) {
			try {
				name = new ObjectName(domain + ":*");
			} catch (MalformedObjectNameException e) {
				// should not reach here
				assert (false);
			}
		}
		Set mbeans = server.queryNames(name, null);
		Map<ObjectName, MBeanInfo> result = new HashMap<ObjectName, MBeanInfo>(mbeans.size());
		Iterator iterator = mbeans.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object instanceof ObjectName) {
				ObjectName o = (ObjectName) object;
				try {
					MBeanInfo info = server.getMBeanInfo(o);
					result.put(o, info);
				} catch (IntrospectionException e) {
					// TODO: should log the error
				} catch (InstanceNotFoundException e) {
					// TODO: should log the error
				} catch (ReflectionException e) {
					// TODO: should log the error
				}
			}
		}
		return result;
	}

	public AttributeList getAttributes(ObjectName name, String[] attributes) throws IOException {
		AttributeList list = null;
		try {
			list = server.getAttributes(name, attributes);
		} catch (InstanceNotFoundException e) {
			// TODO: A MBean may have been unregistered.
			// need to set up listener to listen for MBeanServerNotification.
		} catch (ReflectionException e) {
			// TODO: should log the error
		}
		return list;
	}

	public void setAttribute(ObjectName name, Attribute attribute)
			throws InvalidAttributeValueException, MBeanException, IOException {
		try {
			server.setAttribute(name, attribute);
		} catch (InstanceNotFoundException e) {
			// TODO: A MBean may have been unregistered.
		} catch (AttributeNotFoundException e) {
			assert (false);
		} catch (ReflectionException e) {
			// TODO: should log the error
		}
	}

	public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
			throws IOException, MBeanException {
		Object result = null;
		try {
			result = server.invoke(name, operationName, params, signature);
		} catch (InstanceNotFoundException e) {
			// TODO: A MBean may have been unregistered.
		} catch (ReflectionException e) {
			// TODO: should log the error
		}
		return result;
	}

	public synchronized ClassLoadingMXBean getClassLoadingMXBean() throws IOException {
		if (hasPlatformMXBeans && classLoadingMBean == null) {
			classLoadingMBean = ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class);
		}
		return classLoadingMBean;
	}

	public synchronized CompilationMXBean getCompilationMXBean() throws IOException {
		if (hasCompilationMXBean && compilationMBean == null) {
			compilationMBean = ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.COMPILATION_MXBEAN_NAME, CompilationMXBean.class);
		}
		return compilationMBean;
	}

	@SuppressWarnings("rawtypes")
	public synchronized Collection<GarbageCollectorMXBean> getGarbageCollectorMXBeans()
			throws IOException {

		// TODO: How to deal with changes to the list??
		if (garbageCollectorMBeans == null) {
			ObjectName gcName = null;
			try {
				gcName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
			} catch (MalformedObjectNameException e) {
				// should not reach here
				assert (false);
			}
			Set mbeans = server.queryNames(gcName, null);
			if (mbeans != null) {
				garbageCollectorMBeans = new ArrayList<GarbageCollectorMXBean>();
				Iterator iterator = mbeans.iterator();
				while (iterator.hasNext()) {
					ObjectName on = (ObjectName) iterator.next();
					String name = ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",name=" + on.getKeyProperty("name");

					GarbageCollectorMXBean mBean = ManagementFactory.newPlatformMXBeanProxy(server, name, GarbageCollectorMXBean.class);
					garbageCollectorMBeans.add(mBean);
				}
			}
		}
		return garbageCollectorMBeans;
	}

	@SuppressWarnings("rawtypes")
	public Collection<MemoryPoolMXBean> getMemoryPoolMXBeans() throws IOException {

		// TODO: How to deal with changes to the list??
		if (memoryPoolMXBeans == null) {
			ObjectName poolName = null;
			try {
				poolName = new ObjectName(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",*");
			} catch (MalformedObjectNameException e) {
				// should not reach here
				assert (false);
			}
			Set mbeans = server.queryNames(poolName, null);
			if (mbeans != null) {
				memoryPoolMXBeans = new ArrayList<MemoryPoolMXBean>();
				Iterator iterator = mbeans.iterator();
				while (iterator.hasNext()) {
					ObjectName on = (ObjectName) iterator.next();
					String name = ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",name=" + on.getKeyProperty("name");
					MemoryPoolMXBean mBean = ManagementFactory.newPlatformMXBeanProxy(server, name, MemoryPoolMXBean.class);
					memoryPoolMXBeans.add(mBean);
				}
			}
		}
		return memoryPoolMXBeans;
	}

	public synchronized MemoryMXBean getMemoryMXBean() throws IOException {
		if (hasPlatformMXBeans && memoryMBean == null) {
			memoryMBean = ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
		}
		return memoryMBean;
	}

	public synchronized RuntimeMXBean getRuntimeMXBean() throws IOException {
		if (hasPlatformMXBeans && runtimeMBean == null) {
			runtimeMBean = ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
		}
		return runtimeMBean;
	}

	public synchronized ThreadMXBean getThreadMXBean() throws IOException {
		if (hasPlatformMXBeans && threadMBean == null) {
			threadMBean = ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
		}
		return threadMBean;
	}

	public synchronized OperatingSystemMXBean getOperatingSystemMXBean() throws IOException {
		if (hasPlatformMXBeans && operatingSystemMBean == null) {
			operatingSystemMBean = ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
		}
		return operatingSystemMBean;
	}

	public <T> T getMXBean(ObjectName objName, Class<T> interfaceClass) throws IOException {
		return ManagementFactory.newPlatformMXBeanProxy(server, objName.toString(), interfaceClass);

	}

	public long[] findDeadlockedThreads() throws IOException {
		ThreadMXBean tm = getThreadMXBean();
		if (supportsLockUsage && tm.isSynchronizerUsageSupported()) {
			return tm.findDeadlockedThreads();
		} else {
			return tm.findMonitorDeadlockedThreads();
		}
	}

	public enum ConnectionState {
		DISCONNECTED, CONNECTED, CONNECTING
	}

	public interface AdvancedMBeanServerConnection extends MBeanServerConnection {

		public void flush();

		public boolean isDead();

		public void markAlive();

		public void markDead();
	}

	public static class Advanced {
		private Advanced() {
		}

		public static AdvancedMBeanServerConnection newAdvanced(MBeanServerConnection mbsc) {
			final InvocationHandler ih = new AdvancedInvocationHandler(mbsc);
			return (AdvancedMBeanServerConnection) Proxy.newProxyInstance(Advanced.class.getClassLoader(), new Class[] { AdvancedMBeanServerConnection.class }, ih);
		}
	}

	static class AdvancedInvocationHandler implements InvocationHandler {

		private final MBeanServerConnection conn;

		private Map<ObjectName, NameValueMap> cachedValues = newMap();

		private Map<ObjectName, Set<String>> cachedNames = newMap();

		private volatile boolean isDead = true;

		public void markAlive() {
			isDead = false;
		}

		public void markDead() {
			isDead = true;
		}

		@SuppressWarnings("serial")
		private static final class NameValueMap extends HashMap<String, Object> {
		}

		AdvancedInvocationHandler(MBeanServerConnection conn) {
			this.conn = conn;
		}

		synchronized void flush() {
			cachedValues = newMap();
		}

		public boolean isDead() {
			return isDead;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			final String methodName = method.getName();
			if (methodName.equals("getAttribute")) {
				return getAttribute((ObjectName) args[0], (String) args[1]);
			} else if (methodName.equals("getAttributes")) {
				return getAttributes((ObjectName) args[0], (String[]) args[1]);
			} else if (methodName.equals("flush")) {
				flush();
				return null;
			} else if (methodName.equals("isDead")) {
				return isDead;
			} else if (methodName.equals("markAlive")) {
				markAlive();
				return null;
			} else if (methodName.equals("markDead")) {
				markDead();
				return null;
			} else {
				try {
					return method.invoke(conn, args);
				} catch (InvocationTargetException e) {
					if (isConnException(e.getTargetException())) {
						isDead = true;
					}
					throw e.getCause();
				}
			}
		}

		private Object getAttribute(ObjectName objName, String attrName) throws MBeanException,
				InstanceNotFoundException, AttributeNotFoundException, ReflectionException,
				IOException {
			final NameValueMap values = getCachedAttributes(objName, Collections.singleton(attrName));
			Object value = values.get(attrName);
			if (value != null || values.containsKey(attrName)) {
				return value;
			}
			try {
				return conn.getAttribute(objName, attrName);
			} catch (java.rmi.ConnectException e) {
				isDead = true;
				throw e;
			} catch (java.net.ConnectException e) {
				isDead = true;
				throw e;
			}

		}

		private AttributeList getAttributes(ObjectName objName, String[] attrNames)
				throws InstanceNotFoundException, ReflectionException, IOException {
			final NameValueMap values = getCachedAttributes(objName, new TreeSet<String>(Arrays.asList(attrNames)));
			final AttributeList list = new AttributeList();
			for (String attrName : attrNames) {
				final Object value = values.get(attrName);
				if (value != null || values.containsKey(attrName)) {
					list.add(new Attribute(attrName, value));
				}
			}
			return list;
		}

		private synchronized NameValueMap getCachedAttributes(ObjectName objName,
				Set<String> attrNames) throws InstanceNotFoundException, ReflectionException,
				IOException {
			NameValueMap values = cachedValues.get(objName);
			if (values != null && values.keySet().containsAll(attrNames)) {
				return values;
			}
			attrNames = new TreeSet<String>(attrNames);
			Set<String> oldNames = cachedNames.get(objName);
			if (oldNames != null) {
				attrNames.addAll(oldNames);
			}
			values = new NameValueMap();
			AttributeList attrs = null;

			try {
				attrs = conn.getAttributes(objName, attrNames.toArray(new String[attrNames.size()]));
			} catch (java.rmi.ConnectException e) {
				isDead = true;
				throw e;
			} catch (java.net.ConnectException e) {
				isDead = true;
				throw e;
			}

			for (Attribute attr : attrs.asList()) {
				values.put(attr.getName(), attr.getValue());
			}
			cachedValues.put(objName, values);
			cachedNames.put(objName, attrNames);
			return values;
		}

		private boolean isConnException(Throwable t) {
			if (t instanceof Exception) {
				Exception e = (Exception) t;
				return (e instanceof java.rmi.ConnectException) || (e instanceof java.net.ConnectException);
			}
			return false;
		}

		private static <K, V> Map<K, V> newMap() {
			return new HashMap<K, V>();
		}
	}

	public static void main(String[] args) throws IOException {
		JmxClient inst = JmxClient.getJmxClient(6316);
		inst.connect();
		System.out.println(inst.getMemoryMXBean().getHeapMemoryUsage().getUsed());
		inst.flush();
		
		try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		//inst.connect();
		System.out.println(inst.getMemoryMXBean().getHeapMemoryUsage().getUsed());
		inst.flush();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //inst.connect();
        System.out.println(inst.getMemoryMXBean().getHeapMemoryUsage().getUsed());
		//System.out.println(inst.getMemoryPoolMXBeans().size());
	}

}
