package com.suning.app.spy.core.model;

import java.util.List;

/**
 * @author karry 2014-10-24 下午5:17:32
 */
public class CmdResult {
	
	private String[] cmd;
	
	private List<String> commonResList;
	
	private List<String> errorResList;
	
	public CmdResult(String[] cmd) {
		this.cmd = cmd;
	}

	public String[] getCmd() {
		return cmd;
	}

	public void setCmd(String[] cmd) {
		this.cmd = cmd;
	}

	public List<String> getCommonResList() {
		return commonResList;
	}

	public void setCommonResList(List<String> commonResList) {
		this.commonResList = commonResList;
	}

	public List<String> getErrorResList() {
		return errorResList;
	}

	public void setErrorResList(List<String> errorResList) {
		this.errorResList = errorResList;
	}
	
	

}
