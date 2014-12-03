package com.suning.app.spy.core.command;

import org.apache.log4j.Logger;

import com.suning.app.spy.core.AbstractLifeCycle;
import com.suning.app.spy.core.Agent;
import com.suning.app.spy.core.Configuration;
import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.service.ServiceControl;
import com.suning.app.spy.core.service.impl.CmdOperateService;
import com.suning.app.spy.core.service.impl.JmxOperateService;
import com.suning.app.spy.core.utils.StringHelper;
import com.suning.shared.spy.model.MetricsInfoModel;

/**
 * 类AbstractCommandHandler.java的实现描述
 * 
 * @author karry 2014-10-28 下午6:05:18
 */
public abstract class AbstractCommandHandler<T extends MetricsInfoModel> extends AbstractLifeCycle
		implements CommandHandler, TimerHandler {

	private Configuration _conf;

	private boolean isRunning = false;

	private static CmdOperateService cmdOperateService;

	private static JmxOperateService jmxOperateService;

	private static final String KEY_SPLIT = Constants.KEY_SPLIT;

	private static final String KEY_DELAY_TIME = Constants.KEY_TIMER_DELAY_TIME;

	private static final String KEY_CONTENT = Constants.KEY_COMMAND_CONTENT;

	private static final String VALUE_CONTENT_SEP = Constants.VALUE_CONTENT_SEP;

	private static final Logger LOGGER = Logger.getLogger(AbstractCommandHandler.class);
	
	static {
		cmdOperateService = ServiceControl.getInstance().getBean(CmdOperateService.class);
		jmxOperateService = ServiceControl.getInstance().getBean(JmxOperateService.class);
	}

	protected void doStart(Configuration conf) throws Exception {
		_conf = conf;		
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}
	
	@Override
	public void tagRunning() {
		isRunning = true;
	}
	
	@Override
	public void unTagRunning() {
		isRunning = false;
	}

	@Override
	public long getDelayTime() {

		return Long.parseLong(_conf.get(getCommandName() + KEY_SPLIT + KEY_DELAY_TIME, "0"));
	}

	@Override
	public void handle() {

		try {
			String content = _conf.get(getCommandName() + KEY_SPLIT + KEY_CONTENT);
			if (StringHelper.isBlank(content)) {
				LOGGER.warn("Command content is empty where command is " + this);
				return;
			}
			String[] cmdList = content.split(VALUE_CONTENT_SEP);
			T result = doHandle(cmdList);
			if (null == result) {
				LOGGER.warn("Command execute result is empty where command is " + this);
				return;
			}
			Agent.getInstance().sendResult(result);
		} catch (Exception e) {
			LOGGER.error("Command handler error where command is " + this + ",msg is:" + e.getMessage(), e);
		}

	}

	public String get(String name) {
		return _conf.get(getCommandName() + KEY_SPLIT + name);
	}

	protected CmdOperateService getCmdOperateService() {
		return cmdOperateService;
	}

	protected JmxOperateService getJmxOperateService() {
		return jmxOperateService;
	}

	public abstract T doHandle(String[] cmdList);

	@Override
	public boolean enable() {
		return Boolean.parseBoolean(_conf.get(getCommandName() + KEY_SPLIT + Constants.KEY_COMMAND_ENABLE, "true"));
	}

}
