package com.suning.app.spy.core.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;

/**
 * 类JmxUtil.java的实现描述： JVMTI加载Agent实现本地JMX
 * 
 * @author karry 2014-11-12 上午10:22:20
 */
public abstract class JmxUtil {

	private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

	private static String JAVA_HOME = System.getProperty("java.home");
	
	private static String JVM_SUPPLIER = System.getProperty("java.vm.specification.vendor");

	private static final String CLASS_VIRTUAL_MACHINE = "com.sun.tools.attach.VirtualMachine";

	private static final String CLASS_VIRTUAL_MACHINE_DESCRIPTOR = "com.sun.tools.attach.VirtualMachineDescriptor";

	private static final String CLASS_JMX_REMOTE = "com.sun.management.jmxremote";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String findJMXUrlByProcessId(int pid) {

		if (!isSunJVM()) {
			return StringHelper.EMPTY_STRING;
		}

		String connectorAddress = StringHelper.EMPTY_STRING;

		try {

			File toolsJar = getToolsJar();
			URLClassLoader loader = new URLClassLoader(new URL[] { toolsJar.toURI().toURL() });

			Class virtualMachine = Class.forName(CLASS_VIRTUAL_MACHINE, true, loader);
			Class virtualMachineDescriptor = Class.forName(CLASS_VIRTUAL_MACHINE_DESCRIPTOR, true, loader);

			Method getVMList = virtualMachine.getMethod("list", (Class[]) null);
			Method attachToVM = virtualMachine.getMethod("attach", String.class);
			Method getAgentProperties = virtualMachine.getMethod("getAgentProperties", (Class[]) null);
			Method getVMId = virtualMachineDescriptor.getMethod("id", (Class[]) null);

			List allVMs = (List) getVMList.invoke(null, (Object[]) null);

			for (Object vmInstance : allVMs) {
				String id = (String) getVMId.invoke(vmInstance, (Object[]) null);
				if (id.equals(Integer.toString(pid))) {

					Object vm = attachToVM.invoke(null, id);

					Properties agentProperties = (Properties) getAgentProperties.invoke(vm, (Object[]) null);
					connectorAddress = agentProperties.getProperty(CONNECTOR_ADDRESS);
					break;
				}
			}

			if (StringHelper.isBlank(connectorAddress)) {
				// 尝试让agent加载management-agent.jar
				Method loadAgent = virtualMachine.getMethod("loadAgent", String.class, String.class);
				Method detach = virtualMachine.getMethod("detach", (Class[]) null);
				for (Object vmInstance : allVMs) {
					String id = (String) getVMId.invoke(vmInstance, (Object[]) null);
					if (id.equals(Integer.toString(pid))) {

						Object vm = attachToVM.invoke(null, id);

						File agentJar = getAgentJar();
						if (null == agentJar) {
							throw new IOException("Management agent Jar not found");
						}

						String agent = agentJar.getCanonicalPath();
						loadAgent.invoke(vm, agent, CLASS_JMX_REMOTE);

						Properties agentProperties = (Properties) getAgentProperties.invoke(vm, (Object[]) null);
						connectorAddress = agentProperties.getProperty(CONNECTOR_ADDRESS);

						detach.invoke(vm, (Object[]) null);

						break;
					}
				}
			}

		} catch (Exception ignore) {
			System.err.println(ignore);
		}

		return connectorAddress;
	}

	private static File getToolsJar() {
		String tools = JAVA_HOME + File.separator + "lib" + File.separator + "tools.jar";
		File f = new File(tools);
		if (!f.exists()) {
			tools = JAVA_HOME + File.separator + ".." + File.separator + "lib" + File.separator + "tools.jar";
			f = new File(tools);
		}
		return f;
	}

	private static File getAgentJar() {
		String agent = JAVA_HOME + File.separator + "jre" + File.separator + "lib" + File.separator + "management-agent.jar";
		File f = new File(agent);
		if (!f.exists()) {
			agent = JAVA_HOME + File.separator + "lib" + File.separator + "management-agent.jar";
			f = new File(agent);
			if (!f.exists()) {
				return null;
			}
		}
		return f;
	}

	private static boolean isSunJVM() {
		return JVM_SUPPLIER.equals("Sun Microsystems Inc.") || JVM_SUPPLIER.startsWith("Oracle");
	}

	public static void main(String[] args) {
		System.out.println(getToolsJar());
		System.out.println(getAgentJar());
	}
}