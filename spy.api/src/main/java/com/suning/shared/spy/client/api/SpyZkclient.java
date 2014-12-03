package com.suning.shared.spy.client.api;

import java.util.List;

import org.apache.log4j.Logger;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkClient;
import com.github.zkclient.IZkDataListener;
import com.github.zkclient.ZkClient;
import com.suning.shared.spy.model.MetricsTarget;
import com.suning.shared.spy.model.UnitedMetricsModel;
import com.suning.shared.spy.utils.StringHelper;

/**
 * 类SpyZkclient.java的实现描述 很爽朗的API
 * 
 * @author karry 2014-10-29 下午7:18:17
 */
public class SpyZkclient {

	private String[] zkAddressList;

	private List<MetricsTarget> metricsTargetList;

	private StateListenerInvoker onConnectListenerInvoker;

	private DataListenerInvoker onDataChangeListenerInvoker;

	private StateListenerInvoker onDisConnectListenerInvoker;

	private static final String SEG_ADDRESS = ",";

	private static final String SEG_NODE = "/";

	private IZkClient zkClient;

	private static final Logger LOGGER = Logger.getLogger(SpyZkclient.class);

	private static SpyZkclient ins = new SpyZkclient();

	private SpyZkclient() {

	}

	public static SpyZkclient getInstance() {
		return ins;
	}

	public SpyAttachHolder config(String[] zkAddressList) {
		if (null == zkAddressList || zkAddressList.length < 1) {
			throw new RuntimeException("zk address is invalid!");
		}
		this.zkAddressList = zkAddressList;
		return new SpyAttachHolder();
	}

	private boolean valid(String path, UnitedMetricsModel uModel) {
		return true;
	}

	public void start() {
		// address
		String zkAddress = getZkAddress();
		zkClient = new ZkClient(zkAddress);
		// attach
		for (MetricsTarget metricsTarget : metricsTargetList) {
			String node = SEG_NODE + metricsTarget.getAppName() + SEG_NODE + metricsTarget.getClusterName() + SEG_NODE + metricsTarget.getIp();
			zkClient.subscribeDataChanges(node, new IZkDataListener() {

				@Override
				public void handleDataChange(String path, byte[] data) throws Exception {
					if (StringHelper.isBlank(path) || null == data || data.length < 1) {
						LOGGER.warn("Data change callback param error");
					}

					String dataStr = new String(data,"utf-8");
					UnitedMetricsModel uModel = null;
					try{
//						uModel = JSON.parseObject(dataStr, UnitedMetricsModel.class);
						uModel = UnitedMetricsModel.deserialize(data);
					} catch (Exception e) {
						LOGGER.error("Metrics json data parse error: "+dataStr, e);
					}
					if (valid(path, uModel)) {
						if (null != onDataChangeListenerInvoker) {
							onDataChangeListenerInvoker.invoke(uModel);
						}
					}
				}

				@Override
				public void handleDataDeleted(String path) throws Exception {
					LOGGER.warn("Node has del where path:" + path);
				}

			});

			zkClient.subscribeChildChanges(node, new IZkChildListener() {

				@Override
				public void handleChildChange(String path, List<String> childList) throws Exception {

					MetricsTarget metricsTarget = MetricsTarget.toMetricsTarget(path);
					if (null == metricsTarget) {
						LOGGER.error("their zk state has changed but path is empty!");
						return;
					}
					// disconnect
					if (null == childList) {
						LOGGER.warn("their zk has disconnect where path:" + path);
						if (null != onDisConnectListenerInvoker) {
							onDisConnectListenerInvoker.invoke(metricsTarget);
						}
					} else {
						LOGGER.warn("their zk has connect where path:" + path);
						if (null != onConnectListenerInvoker) {
							onConnectListenerInvoker.invoke(metricsTarget);
						}
					}
				}

			});
		}

	}

	private String getZkAddress() {
		StringBuilder sb = new StringBuilder(StringHelper.EMPTY);

		for (String metaAddress : zkAddressList) {
			sb.append(metaAddress).append(SEG_ADDRESS);
		}
		sb.deleteCharAt(sb.length() - 1);
		LOGGER.info("zk address is:" + sb.toString());
		return sb.toString();
	}

	public class SpyAttachHolder {

		public SpyListenerHolder attach(List<MetricsTarget> metricsTargetList) {
			if (null == metricsTargetList || metricsTargetList.size() < 1) {
				throw new RuntimeException("ZK attach blank!");
			}
			SpyZkclient.this.metricsTargetList = metricsTargetList;
			return new SpyListenerHolder();
		}

		public void start() {

			SpyZkclient.this.start();
		}

	}

	public class SpyListenerHolder {

		public SpyListenerHolder onConnect(StateListenerInvoker listenerInvoker) {
			SpyZkclient.this.onConnectListenerInvoker = listenerInvoker;
			return this;
		}

		public SpyListenerHolder onDataChange(DataListenerInvoker listenerInvoker) {
			SpyZkclient.this.onDataChangeListenerInvoker = listenerInvoker;
			return this;
		}

		public SpyListenerHolder onDisConnect(StateListenerInvoker listenerInvoker) {
			SpyZkclient.this.onDisConnectListenerInvoker = listenerInvoker;
			return this;
		}

		public void start() {
			SpyZkclient.this.start();
		}

	}

	public interface DataListenerInvoker {

		public void invoke(UnitedMetricsModel model);
	}

	public interface StateListenerInvoker {

		public void invoke(MetricsTarget target);
	}

}
