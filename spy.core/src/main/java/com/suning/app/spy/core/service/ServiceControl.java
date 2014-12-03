package com.suning.app.spy.core.service;

import java.util.ServiceLoader;

import com.suning.app.spy.core.AggregateLifeCycle;
import com.suning.app.spy.core.Configuration;

/**
 * 类ServiceControl.java的实现描述：
 * 
 * @author karry 2014-10-27 上午11:42:42
 */
public class ServiceControl extends AggregateLifeCycle {

	
	private static ServiceControl inst = new ServiceControl();
	
	private ServiceControl() {
		
	}
	
	public static ServiceControl getInstance() {
		return inst;
	}
	
	@Override
    protected void doStart(Configuration conf) throws Exception {
		
		for (Service service : ServiceLoader.load(Service.class,
				ServiceControl.class.getClassLoader())) {
			addBean(service);
		}
		super.doStart(conf);	
	}

}
