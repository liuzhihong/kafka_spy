package com.suning.app.spy.core.utils;

import java.net.InetAddress;

/**
 * 类MachineInfoUtil.java的实现描述
 * @author karry 2014-10-29 下午3:18:33
 */
public abstract class MachineInfoUtil {
	
	protected static String    machineIp = getHostIp();
	
	private static String getHostIp() {
        String localhost = "";
        try {
            localhost = InetAddress.getByName(getHostName()).getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localhost;
    }
	
	private static String getHostName() {
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hostName;
    }
	
	public static String getMachineIp() {
        return machineIp;
    }
	
	public static void main(String[] args) {
		System.out.println(MachineInfoUtil.getHostIp());
	}

}
