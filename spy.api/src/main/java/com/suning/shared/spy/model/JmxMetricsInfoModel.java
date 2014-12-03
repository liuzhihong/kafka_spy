package com.suning.shared.spy.model;


public abstract class JmxMetricsInfoModel extends MetricsInfoModel {
	
	private static final long serialVersionUID = -1101544228427465323L;

	public String host;
	
	public String port;
	
	public String username;
	
	public String password;
	
	public String pidName;
	
	public boolean target; //是否为目标进程

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}

    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }

    public String getPidName() {
        return pidName;
    }

    public boolean isTarget() {
        return target;
    }
    
    ////////////////////////write//////////////////////////

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPidName(String pidName) {
        this.pidName = pidName;
    }

    public void setTarget(boolean target) {
		this.target = target;
	}
    
    

}
