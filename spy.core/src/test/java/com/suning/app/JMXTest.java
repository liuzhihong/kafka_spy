package com.suning.app;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.suning.app.spy.core.utils.JmxUtil;

public class JMXTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		String jmxUrl  = JmxUtil.findJMXUrlByProcessId(2731);
		JMXConnector jmxc = JMXConnectorFactory.connect(new JMXServiceURL(jmxUrl));
		
		System.out.println("MBeanConn:"+jmxc.getMBeanServerConnection().getMBeanCount());
		System.out.println("connId:"+jmxc.getConnectionId());
		

	}

}
