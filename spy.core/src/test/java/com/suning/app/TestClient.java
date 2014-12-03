package com.suning.app;

import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("unused")
public class TestClient {
	
	public static String constant = "123";
	
	private  static Map<String,Date> repos = new HashMap<String,Date>();
	
	private  Date currentDate = new Date();
			
	
	private static TestClient self = new TestClient();
	
	public static TestClient getInstance() {
		return self;
	}
	
	{
		for (int i=0;i< 20;i++) {
			repos.put(""+i, new Date());
		}

	}
	
	@SuppressWarnings("deprecation")
	private  String getCurrentDate() {
		return currentDate.toGMTString();
	}
	

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println(TestClient.getInstance().toString());
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ClassLoadingMXBean clMBean = ManagementFactory.getClassLoadingMXBean();
		clMBean.setVerbose(true);
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					//System.out.println("cl:"+cl);
					//Class t = Class.forName("com.suning.tools.frostmourne.agent.AgentClassLoader",true,cl);
					//System.gc();
				} catch (Exception e) {
					//System.out.println("class not fount");
					return;
				}	
				//System.out.println("class has fount");
			}

		}, 0, 20000);
				
		System.in.read();
	}

}
