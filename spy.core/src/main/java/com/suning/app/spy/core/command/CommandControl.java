package com.suning.app.spy.core.command;

import java.util.ServiceLoader;

import com.suning.app.spy.core.AggregateLifeCycle;
import com.suning.app.spy.core.Configuration;

/**
 * 类CommandControl.java的实现描述
 * @author karry 2014-10-28 下午6:05:26
 */
public class CommandControl extends AggregateLifeCycle {


	private static CommandControl inst = new CommandControl();

	public static CommandControl getInstance() {

		return inst;
	}

	private CommandControl() {

	}

	@Override
	protected void doStart(Configuration conf) throws Exception {

		for (CommandHandler handler : ServiceLoader.load(CommandHandler.class, CommandControl.class.getClassLoader())) {
			addBean(handler);
		}
		super.doStart(conf);
	}

}
