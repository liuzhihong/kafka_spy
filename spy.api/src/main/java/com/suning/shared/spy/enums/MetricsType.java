package com.suning.shared.spy.enums;

import com.suning.shared.spy.utils.StringHelper;


/**
 * 类MetricsType.java的实现描述
 * @author karry 2014-10-29 下午2:09:20
 */
public enum MetricsType {
	
	//VM Feature
	CPU("cpu"),
	MEM("mem"),
	DISK("disk"),
	NET("net"),
	//JMX Monitor Feature
	GC("gc"),
	HEAP("heap"),
	META_GC("meta_gc"),
	META_HEAP("meta_heap");
	
	private String name;
	
	MetricsType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public static MetricsType toMetricsType(String name) {
		
		for (MetricsType type: MetricsType.values()) {
			if (StringHelper.equals(type.getName(), name)) {
				return type;
			}
		}
		return null;
	}
	
	public static int size() {
		return MetricsType.values().length;
	}
}
