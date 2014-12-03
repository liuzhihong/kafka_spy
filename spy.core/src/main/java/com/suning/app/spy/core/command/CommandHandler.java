package com.suning.app.spy.core.command;

/**
 * @author karry 2014-10-24 下午5:24:43
 */
public interface CommandHandler {
		
	public void handle();
	
	public boolean enable();
	
	public String getCommandName();
	
}
