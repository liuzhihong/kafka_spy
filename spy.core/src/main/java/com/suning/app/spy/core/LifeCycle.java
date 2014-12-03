package com.suning.app.spy.core;

/**
 * @author karry 2014-10-24 上午11:57:41
 */
public interface LifeCycle {

	public void start(Configuration conf);
	
	public boolean isStarting();

	public void stop();

	public void restart();
	
	
}
