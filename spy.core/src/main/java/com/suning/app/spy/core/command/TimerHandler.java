package com.suning.app.spy.core.command;

/**
 * @author karry 2014-10-24 下午5:26:42
 */
public interface TimerHandler {

	public boolean isRunning();
	
	public void tagRunning();
	
	public void unTagRunning();

	public long getDelayTime();

}
