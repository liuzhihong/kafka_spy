package com.suning.shared.spy.model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

import com.suning.shared.spy.enums.MetricsType;

/**
 * CPU模型类 默认时间单位为jiffies 1jiffies=0.01秒,该模型以秒为单位 linux命令：cat /proc/cpuinfo|grep 'processor'|wc -l && uptime && cat
 * /proc/stat 从系统启动开始累计到当前时刻的CPU时间
 * 
 * @author 13073050
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class CpuInfoModel extends MetricsInfoModel {

    private static final long serialVersionUID = 2421919343450730786L;

    // field
    public int cpuNumber;// cpu核数

    public float[] loadValue;

    public List<CpuModel> cpuList;

    // getter
    public int getCpuNumber() {
        return cpuNumber;
    }

    public float[] getLoadValue() {
        return loadValue;
    }

    public List<CpuModel> getCpuList() {
        return cpuList;
    }

	@Override
	public MetricsType getType() {
		return MetricsType.CPU;
	}
	/**
	 * 
	 * 属性里面的时间存放的都是两次取样之间的差值
	 *
	 * @author 13073050
	 * @see [相关类/方法]（可选）
	 * @since [产品/模块版本] （可选）
	 */
    public static class CpuModel implements Serializable{

		private static final long serialVersionUID = -5256702356456181987L;

		public long userTime;// 用户态时间

		public long niceTime;

		public long sysTime;// 内核时间

		public long idleTime;// 空闲时间
		
		public long iowaitTime;   //io等待时间
		
		public long irqTime;//硬中断时间
		
		public long softirqTime;//软中断时间
		
		public long stealstolenTime;
		
		public long guestTime;
		
		public long totalCpuTime;

        public long getUserTime() {
            return userTime;
        }

        public void setUserTime(long userTime) {
            this.userTime = userTime;
        }

        public long getNiceTime() {
            return niceTime;
        }

        public void setNiceTime(long niceTime) {
            this.niceTime = niceTime;
        }

        public long getSysTime() {
            return sysTime;
        }

        public void setSysTime(long sysTime) {
            this.sysTime = sysTime;
        }

        public long getIdleTime() {
            return idleTime;
        }

        public void setIdleTime(long idleTime) {
            this.idleTime = idleTime;
        }

        public long getIowaitTime() {
            return iowaitTime;
        }

        public void setIowaitTime(long iowaitTime) {
            this.iowaitTime = iowaitTime;
        }

        public long getIrqTime() {
            return irqTime;
        }

        public void setIrqTime(long irqTime) {
            this.irqTime = irqTime;
        }

        public long getSoftirqTime() {
            return softirqTime;
        }

        public void setSoftirqTime(long softirqTime) {
            this.softirqTime = softirqTime;
        }

        public long getStealstolenTime() {
            return stealstolenTime;
        }

        public void setStealstolenTime(long stealstolenTime) {
            this.stealstolenTime = stealstolenTime;
        }

        public long getGuestTime() {
            return guestTime;
        }

        public void setGuestTime(long guestTime) {
            this.guestTime = guestTime;
        }

        public void setTotalCpuTime(long totalCpuTime) {
            this.totalCpuTime = totalCpuTime;
        }

        public long getTotalCpuTime() {
            return totalCpuTime;
        }

        /**
         * 功能描述: <br>
         * 计算CPU的利用率
         * 
         * @return
         * @see [相关类/方法](可选)
         * @since [产品/模块版本](可选)
         */
        public String getCpuUsage() {
            double cpuUsage = 100*(double) (this.getTotalCpuTime()-this.getIdleTime())
                    / (double) (this.getTotalCpuTime());
            return new DecimalFormat("0.00").format(cpuUsage);
        }

        /**
         * 功能描述: 计算用户态CPU利用率
         * 
         * @return
         * @see [相关类/方法](可选)
         * @since [产品/模块版本](可选)
         */
        public String getUserCpuUsage() {
            double userUsage = 100*(double) (this.getUserTime())
                    / (double) (this.getTotalCpuTime());
            return new DecimalFormat("0.00").format(userUsage);
        }

        /**
         * 功能描述:计算系统CPU利用率
         * 
         * @return
         * @see [相关类/方法](可选)
         * @since [产品/模块版本](可选)
         */
        public String getSysCpuUsage() {
            double sysUsage = 100*(double) (this.getSysTime())
                    / (double) (this.getTotalCpuTime());
            return new DecimalFormat("0.00").format(sysUsage);
        }
        
    }

    // ////////write/////////////

    public void setCpuNumber(int cpuNumber) {
        this.cpuNumber = cpuNumber;
    }

    public void setLoadValue(float[] loadValue) {
        this.loadValue = loadValue;
    }

    public void setCpuList(List<CpuModel> cpuList) {
        this.cpuList = cpuList;
    }

}
