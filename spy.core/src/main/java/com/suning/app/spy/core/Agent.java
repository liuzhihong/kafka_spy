package com.suning.app.spy.core;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.suning.app.spy.core.command.CommandControl;
import com.suning.app.spy.core.command.CommandHandler;
import com.suning.app.spy.core.command.TimerHandler;
import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.service.ServiceControl;
import com.suning.app.spy.core.service.impl.ZookeeperOperateService;
import com.suning.app.spy.core.utils.MachineInfoUtil;
import com.suning.app.spy.core.utils.StringHelper;
import com.suning.shared.spy.model.MetricsInfoModel;
import com.suning.shared.spy.model.MetricsTarget;
import com.suning.shared.spy.model.UnitedMetricsModel;

/**
 * 类Agent.java的实现描述：
 * 
 * @author karry 2014-10-28 上午11:33:54
 */
public class Agent {

	private static final String COMMAND_CONF_FILE = Constants.COMMAND_CONF_FILE;

	private static final String SERVICE_CONF_FILE = Constants.SERVICE_CONF_FILE;

	private static final Configuration commandConf = new Configuration(COMMAND_CONF_FILE);

	private static final Configuration serviceConf = new Configuration(SERVICE_CONF_FILE);

	private static final Logger LOGGER = Logger.getLogger(Agent.class);

	private UnitedMetricsModel unitedMetricsModel;

	private final Lock lock = new ReentrantLock();

	private static long sendInterval;

	private static String localIp;

	private static String appName;

	private static String clusterName;

	private static Agent inst = new Agent();

	private ZookeeperOperateService zkOperateService;

	private Agent() {

	}

	public static Agent getInstance() {
		return inst;
	}

	public void sendResult(MetricsInfoModel resultInfo) {

		try {
			lock.lock();
			LOGGER.debug("send result where result:" + JSON.toJSONString(resultInfo));
			unitedMetricsModel.setMetricsModel(resultInfo);
		} finally {
			lock.unlock();
		}

	}

	public void doSend(UnitedMetricsModel model) {
		if (null == model || model.size() < 1) {
			return;
		}
		String json = JSON.toJSONString(model, SerializerFeature.WriteClassName);
		if (StringHelper.isBlank(json)) {
			LOGGER.warn("JSON STR is empty where model:" + model.toString());
			return;
		}
	}

	public void init() {
		ServiceControl.getInstance().start(serviceConf);
		if (!ServiceControl.getInstance().isStarting()) {
			LOGGER.error("ServiceControl start eror!");
			System.exit(-1);
		}
		CommandControl.getInstance().start(commandConf);
		if (!CommandControl.getInstance().isStarting()) {
			LOGGER.error("CommandControl start eror!");
			System.exit(-1);
		}
		//
		localIp = MachineInfoUtil.getMachineIp();
		appName = serviceConf.get(Constants.KEY_APP_NAME, "UndefinedApp");
		clusterName = serviceConf.get(Constants.KEY_CLUSTER_NAME, "UndefinedCluster");
		sendInterval = Long.parseLong(serviceConf.get(Constants.KEY_SEND_INTERVAL, "30000"));
		MetricsTarget metricsTarget = new MetricsTarget(appName, clusterName, localIp);
		unitedMetricsModel = new UnitedMetricsModel(metricsTarget);

		zkOperateService = ServiceControl.getInstance().getBean(ZookeeperOperateService.class);
		
		if (!zkOperateService.postMetricsTarget(metricsTarget)) {
			LOGGER.error("zk init error");
			System.exit(-1);
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						lock.lock();
						if (null != unitedMetricsModel && unitedMetricsModel.size() > 0) {
							zkOperateService.Send(unitedMetricsModel);
						}
					} finally {
						unitedMetricsModel.clear();
						lock.unlock();
					}

					try {
						Thread.sleep(sendInterval);
					} catch (InterruptedException e) {
					}
					LOGGER.info("Send once!");
				}

			}

		}).start();

		LOGGER.info("Agent init succeed!");
	}

	public void fireCommand() {

		List<CommandHandler> commandHandlers = CommandControl.getInstance().getBeans(CommandHandler.class);
		if (null == commandHandlers || commandHandlers.size() < 1) {
			LOGGER.info("Has no command,so exit!");
			System.exit(0);
		} else {

			for (final CommandHandler commandHandler : commandHandlers) {
				if (!commandHandler.enable()) {
					LOGGER.info("Command is disabled where command:" + commandHandler.getCommandName());
					continue;
				}
				if (commandHandler instanceof TimerHandler) {
					final TimerHandler tHandler = (TimerHandler) commandHandler;
					long delayTime = tHandler.getDelayTime();

					if (delayTime > 0) {
						new Timer().schedule(new TimerTask() {

							@Override
							public void run() {
								if (tHandler.isRunning()) {
									LOGGER.warn("Command is running so task skiped where command is " + commandHandler);
									return;
								}
								try {
									tHandler.tagRunning();
									commandHandler.handle();
								}finally {
									tHandler.unTagRunning();
								}																
							}

						}, 0, delayTime);
					} else {
						commandHandler.handle();
					}

				} else {
					commandHandler.handle();
				}
			}
		}

		LOGGER.info("Agent fire command succeed!");

	}

	public static void main(String[] args) {
		Agent agent = Agent.getInstance();
		agent.init();
		agent.fireCommand();
	}
}
