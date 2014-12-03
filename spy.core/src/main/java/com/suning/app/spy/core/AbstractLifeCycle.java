package com.suning.app.spy.core;

import org.apache.log4j.Logger;

import com.suning.app.spy.core.constants.Constants;

/**
 * 类AbstractLifeCycle.java的实现描述：
 * @author karry 2014-10-27 上午11:14:39
 */
public abstract class AbstractLifeCycle implements LifeCycle {

	private final int __FAIL = -1, __INIT = 0, __STARTING = 1, __STOP = 2;

	private int _state = __INIT;

	private static final Logger LOGGER = Logger.getLogger(AbstractLifeCycle.class);
	
	/**
	 * 重试的间隔毫秒数
	 */
	private static  long interval;
	
	/**
	 * 每次重试的次数
	 */
	private static  int retry_count_limit;
	
	private static Configuration commonConf = new Configuration(Constants.COMMON_CONF_FILE);
	
	static {
		interval = Long.parseLong(commonConf.get(Constants.KEY_RETRY_INTERVAL, "30000"));
		retry_count_limit = Integer.parseInt(commonConf.get(Constants.KEY_RETRY_COUNT_LIMIT, "3"));
	}

	@Override
	public void start(Configuration conf) {

		LOGGER.info("Start " + this);
				

		if (_state == __STARTING) {
			return;
		}

		try {
			doStart(conf);
			_state = __STARTING;
		} catch (Exception e) {
			setFailed("Start",e);
			
		}
		if (!isStarting()) {
			restart();
		}

	}

	protected void doStart(Configuration conf) throws Exception {
	}

	@Override
	public boolean isStarting() {

		return _state == __STARTING;
	}

	@Override
	public void stop() {
		LOGGER.info("Stop " + this);

		try {
			doStop();
			_state = __STOP;
		} catch (Exception e) {
			setFailed("Stop",e);
		}

	}

	protected void doStop() throws Exception {
	}

	@Override
	public void restart() {
		
		int restart_count = 0;
		LOGGER.info("Restart " + this);
		
		while(!isStarting() && restart_count < retry_count_limit ) {
			restart_count ++;
			try {
				doRestart();
				_state = __STARTING;
			} catch (Exception e) {
				setFailed("Restart[count:"+restart_count+"]",e);
			}
			
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
	            LOGGER.error("The sleeping thread occured an InterruptedException,retry aborted.", e);
	            break;
			}
		}
		
	}

	protected void doRestart() throws Exception {
	}

	protected void setFailed(String metaInf,Throwable th) {
		_state = __FAIL;
		LOGGER.warn(metaInf+" Failed " + this + ": " + th, th);
	}
	
	protected void setFailed(String metaInf) {
		_state = __FAIL;
		LOGGER.warn(metaInf+" Failed " + this);
	}

}
