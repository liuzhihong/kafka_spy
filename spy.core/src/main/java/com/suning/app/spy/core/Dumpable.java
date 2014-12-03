package com.suning.app.spy.core;

import java.io.IOException;

/**
 * 类Dumpable.java的实现描述：
 * 
 * @author karry 2014-10-27 上午11:18:46
 */
public interface Dumpable {
	
	void dump(Appendable out, String indent) throws IOException;
}