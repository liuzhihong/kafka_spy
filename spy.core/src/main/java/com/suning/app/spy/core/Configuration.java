package com.suning.app.spy.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.utils.StringHelper;

/**
 * 类Configuration.java的实现描述：配置管理
 * 
 * @author karry 2014-10-27 下午2:31:51
 */
public class Configuration implements Iterable<Map.Entry<String, String>> {

	private static final Logger LOGGER = Logger.getLogger(Configuration.class);

	private static final String UNKNOWN_RESOURCE = "UnknownResource";

	private static final WeakHashMap<Configuration, Object> REGISTRY = new WeakHashMap<Configuration, Object>();

	private static final CopyOnWriteArrayList<String> defaultResources = new CopyOnWriteArrayList<String>();

	static {
		addDefaultResource(Constants.COMMON_CONF_FILE);
	}

	private ArrayList<Object> resources = new ArrayList<Object>();

	private boolean loadDefaults = true;

	private HashMap<String, String> updatingResource;

	private Properties properties;

	private ClassLoader classLoader;

	{
		classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = Configuration.class.getClassLoader();
		}
	}

	public Configuration() {
		this(true);
	}

	public Configuration(boolean loadDefaults) {
		this.loadDefaults = loadDefaults;
		updatingResource = new HashMap<String, String>();
		synchronized (Configuration.class) {
			REGISTRY.put(this, null);
		}
	}

	public Configuration(String... _resources) {
		this(true);
		for (String resource : _resources) {
			addResource(resource);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Configuration(Configuration other) {
		this.resources = (ArrayList) other.resources.clone();
		synchronized (other) {
			if (other.properties != null) {
				this.properties = (Properties) other.properties.clone();
			}

			this.updatingResource = new HashMap<String, String>(other.updatingResource);
		}

		synchronized (Configuration.class) {
			REGISTRY.put(this, null);
		}
	}

	public static synchronized void addDefaultResource(String name) {
		if (!defaultResources.contains(name)) {
			defaultResources.add(name);
			for (Configuration conf : REGISTRY.keySet()) {
				if (conf.loadDefaults) {
					conf.reloadConfiguration();
				}
			}
		}
	}

	public void addResource(String name) {
		addResourceObject(name);
	}

	private synchronized void addResourceObject(Object resource) {
		resources.add(resource); // add to resources
		reloadConfiguration();
	}

	public synchronized void reloadConfiguration() {
		properties = null; // trigger reload
	}

	@SuppressWarnings("rawtypes")
	private void loadResources(Properties properties, ArrayList resources) {
		if (loadDefaults) {
			for (String resource : defaultResources) {
				loadResource(properties, resource);
			}
		}

		for (Object resource : resources) {
			loadResource(properties, resource);
		}
	}

	private void loadResource(Properties properties, Object name) {

		if (null == properties) {
			properties = new Properties();
		}
		if (name instanceof String) {
			try {
				properties.load(this.getClass().getResourceAsStream((String) name));
			} catch (IOException e) {
				LOGGER.warn("Load resource failed where resource name:" + name);
			}
		}
	}

	private synchronized Properties getProps() {
		if (properties == null) {
			properties = new Properties();
			loadResources(properties, resources);
		}
		return properties;
	}

	public String get(String name) {

		return StringHelper.trim(getProps().getProperty(name));
	}

	public String get(String name, String defaultValue) {

		return StringHelper.trim(getProps().getProperty(name, defaultValue));
	}

	public void set(String name, String value) {
		getProps().setProperty(name, value);
		this.updatingResource.put(name, UNKNOWN_RESOURCE);
	}

	public synchronized void clear(String name) {
		getProps().remove(name);
	}

	public int size() {
		return getProps().size();
	}

	public void clear() {
		getProps().clear();
	}

	@Override
	public Iterator<Entry<String, String>> iterator() {
		Map<String, String> result = new HashMap<String, String>();
		for (Map.Entry<Object, Object> item : getProps().entrySet()) {
			if (item.getKey() instanceof String && item.getValue() instanceof String) {
				result.put((String) item.getKey(), (String) item.getValue());
			}
		}
		return result.entrySet().iterator();
	}

}
