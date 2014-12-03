package com.suning.shared.spy.model;

import java.io.Serializable;

import com.suning.shared.spy.utils.StringHelper;

/**
 * 类MetricsTarget.java的实现描述：性能指标实体
 * 
 * @author karry 2014-10-29 下午7:32:36
 */
public class MetricsTarget implements Serializable{

	private static final long serialVersionUID = 4163503537176604779L;

	private String appName;

	private String clusterName;

	private String ip;
	
	public MetricsTarget() {
		
	}

	public MetricsTarget(String appName, String clusterName, String ip) {
		this.appName = appName;
		this.clusterName = clusterName;
		this.ip = ip;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public static MetricsTarget toMetricsTarget(String path) {
		if (!StringHelper.isBlank(path)) {
			String[] metaList = path.split("/");
			if (null != metaList && metaList.length == 4) {
				return new MetricsTarget(metaList[1], metaList[2], metaList[3]);
			}
		}
		return null;
	}

	public String toPath() {
		String path = (StringHelper.isBlank(appName) ? "" : ("/" + appName)) + (StringHelper.isBlank(clusterName) ? "" : ("/" + clusterName)) + (StringHelper.isBlank(ip) ? "" : ("/" + ip));
		return path;
	}

	public String toRootPath() {
		String rootPath = (StringHelper.isBlank(appName) ? "" : ("/" + appName)) + (StringHelper.isBlank(clusterName) ? "" : ("/" + clusterName));
		return rootPath;
	}
	
	@Override
	public String toString() {
		return toPath();
	}
	
	public static void main(String[] args) {
		MetricsTarget target = new MetricsTarget("1","","");
		System.out.println(target.toPath()+","+target.toRootPath());
	}

}
