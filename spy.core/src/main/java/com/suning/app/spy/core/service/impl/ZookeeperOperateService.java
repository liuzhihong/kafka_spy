package com.suning.app.spy.core.service.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.github.zkclient.IZkClient;
import com.github.zkclient.IZkDataListener;
import com.github.zkclient.IZkStateListener;
import com.github.zkclient.ZkClient;
import com.suning.app.spy.core.AbstractLifeCycle;
import com.suning.app.spy.core.Configuration;
import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.service.Service;
import com.suning.app.spy.core.utils.StringHelper;
import com.suning.shared.spy.model.MetricsTarget;
import com.suning.shared.spy.model.UnitedMetricsModel;

public class ZookeeperOperateService extends AbstractLifeCycle implements Service {

	private static final Logger LOGGER = Logger.getLogger(ZookeeperOperateService.class);

	private IZkClient zkClient;

	private String addressList;

	@SuppressWarnings("unused")
	private String charSet;

	public boolean creatNode(MetricsTarget metricsTarget) {

		String rootPath = metricsTarget.toRootPath();

		if (!zkClient.exists(rootPath)) {
			zkClient.createPersistent(rootPath, true);
		}
		if (!zkClient.exists(metricsTarget.toPath())) {
			zkClient.createEphemeral(metricsTarget.toPath());
		}

		return true;
	}

	public boolean postMetricsTarget(final MetricsTarget metricsTarget) {

		if (null == metricsTarget || StringHelper.isBlank(metricsTarget.getIp())) {
			LOGGER.warn("init error becauseof blank metricsTarget:" + metricsTarget.toString());
			return false;
		}

		creatNode(metricsTarget);

		zkClient.subscribeStateChanges(new IZkStateListener() {

			@Override
			public void handleNewSession() throws Exception {
				LOGGER.info("New Session");
			}

			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				switch (state) {
				case Disconnected:
					LOGGER.warn("zk disconnected! ");
					break;
				case SyncConnected:
					LOGGER.warn("zk reconnected! ");
					creatNode(metricsTarget);
					break;
				default:
					LOGGER.warn("zk change state:" + state);
				}

			}

		});
		return true;
	}

	public void Send(UnitedMetricsModel model) {
		if (null == model) {
			return;
		}
		MetricsTarget metricsTarget = model.getMetricsTarget();
		String path = (null == metricsTarget ? "" : metricsTarget.toPath());
		if (StringHelper.isBlank(path)) {
			LOGGER.warn("Send failed becauseof path is empty!");
			return;
		}
		//TODO:待Fastjson反序列化问题解决后使用	
//		String json = StringHelper.EMPTY_STRING;
//		json = JSON.toJSONString(model, SerializerFeature.WriteClassName);
//		if (StringHelper.isBlank(json)) {
//			LOGGER.warn("Send failed becauseof JSON STR is empty where model:" + model.toString());
//			return;
//		}
		

		try {
			zkClient.writeData(path, UnitedMetricsModel.serialize(model));
		} catch (Exception e) {
			LOGGER.warn("Serialize error!", e);
		}
	}

	@Override
	protected void doStart(Configuration conf) {

		// can sep with Constants.VALUE_CONTENT_SEP
		addressList = conf.get(Constants.KEY_ZK_ADDRESS);
		charSet = conf.get(Constants.KEY_CHAR_SET, "UTF-8");
		if (StringHelper.isBlank(addressList)) {
			LOGGER.warn("zkAddress is blank!");
			setFailed("ZK service start");
			return;
		}
		zkClient = new ZkClient(addressList);
		if (!zkClient.isConnected()) {
			LOGGER.warn("zk can not connect!");
			setFailed("ZK service start");
			return;
		}

	}

	@Override
	protected void doStop() {

		if (null != zkClient) {
			zkClient.unsubscribeAll();
			zkClient.close();
			zkClient = null;
		}
	}

	@Override
	protected void doRestart() {
		doStop();
		zkClient = new ZkClient(addressList);
		if (!zkClient.isConnected()) {
			LOGGER.warn("zk can not connect!");
			setFailed("ZK service start");
			return;
		}
	}

	public static void main(String[] args) throws IOException {

		final IZkClient zkClient = new ZkClient("127.0.0.1:2181");
		final String node = "/mwbops/kafka/127.0.0.1/cpu";

		zkClient.subscribeStateChanges(new IZkStateListener() {

			@Override
			public void handleNewSession() throws Exception {
				System.out.println("New Session");

			}

			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				System.out.println("state change! " + state);
				System.out.println("node exit " + zkClient.exists(node));

			}

		});

		zkClient.subscribeDataChanges("/mwbops/kafka/127.0.0.1/cpu", new IZkDataListener() {

			@Override
			public void handleDataChange(String arg0, byte[] arg1) throws Exception {
				System.out.println("Data changes:" + new String(arg1) + ",node:" + arg0);

			}

			@Override
			public void handleDataDeleted(String arg0) throws Exception {
				System.out.println("Data del:" + arg0);

			}

		});

		zkClient.createPersistent("/mwbops/kafka/127.0.0.1", true);

		zkClient.createEphemeral(node);

		zkClient.writeData(node, "test".getBytes());

		zkClient.delete(node);

		System.in.read();
	}

}
