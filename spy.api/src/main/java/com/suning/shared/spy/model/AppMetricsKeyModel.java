/*
 * Copyright (C), 2002-2014, 苏宁易购电子商务有限公司
 * FileName: AppMetricsKeyModel.java
 * Author:   13073050
 * Date:     2014年11月12日 下午9:50:21
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.suning.shared.spy.model;

import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br>
 * 〈功能详细描述〉
 * 
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class AppMetricsKeyModel extends MetricsTarget implements Serializable {

	/**
     */
	private static final long serialVersionUID = -568858023393664681L;

	/**
     */
	private boolean target;

	public AppMetricsKeyModel() {
		// TODO Auto-generated constructor stub
	}

	public AppMetricsKeyModel(String appName, String clusterName, String ip) {
		super(appName, clusterName, ip);
		// TODO Auto-generated constructor stub
	}

	public boolean isTarget() {
		return target;
	}

	public void setTarget(boolean target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return getAppName() + "/" + getClusterName() + "/" + getIp() + "/" + isTarget();
	}

	public static AppMetricsKeyModel toAppMetricsKeyModel(String str) {
		AppMetricsKeyModel model = new AppMetricsKeyModel();
		String[] ele = str.split("/");
		if (null == ele || ele.length < 4) {
			return model;
		}
		model.setAppName(ele[0]);
		model.setClusterName(ele[1]);
		model.setIp(ele[2]);
		model.setTarget(Boolean.parseBoolean(ele[3]));
		return model;
	}
}
