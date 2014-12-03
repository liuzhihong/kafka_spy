package com.suning.shared.spy.model;

import java.io.Serializable;
import java.util.Date;

import com.suning.shared.spy.enums.MetricsType;

/**
 * 类MetricsInfoModel.java的实现描述
 * @author karry 2014-10-29 下午2:31:55
 */
public abstract class MetricsInfoModel implements Serializable{

	private static final long serialVersionUID = -5464223452424414376L;
	
	public Date collectTime;
	
	public MetricsInfoModel() {
		collectTime = new Date();
	}

	public Date getCollectTime() {
		return collectTime;
	}
	
	public abstract MetricsType getType();

}
