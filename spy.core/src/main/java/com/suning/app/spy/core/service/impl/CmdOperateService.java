package com.suning.app.spy.core.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.suning.app.spy.core.AbstractLifeCycle;
import com.suning.app.spy.core.Configuration;
import com.suning.app.spy.core.constants.Constants;
import com.suning.app.spy.core.model.CmdResult;
import com.suning.app.spy.core.service.Service;

/**
 * 类CmdOperateService.java的实现描述
 * @author karry 2014-10-28 下午5:35:35
 */
public class CmdOperateService extends AbstractLifeCycle implements Service {

	private static final Logger LOGGER = Logger.getLogger(CmdOperateService.class);

	public static ExecutorService callTimeoutPool = Executors.newFixedThreadPool(1);

	public static long callTimeout = 2000;

	private static final String KEY_COMMAND_EXEC_THREAD_COUNT = Constants.KEY_COMMAND_EXEC_THREAD_COUNT;

	private static final String KEY_COMMAND_EXEC_TIME_OUT = Constants.KEY_COMMAND_EXEC_TIME_OUT;

	@Override
	public void doStart(Configuration conf) {

		int commandExecThreadCount = Integer.parseInt(conf.get(KEY_COMMAND_EXEC_THREAD_COUNT, "1"));
		callTimeout = Long.parseLong(conf.get(KEY_COMMAND_EXEC_TIME_OUT, "3000"));
		callTimeoutPool = Executors.newFixedThreadPool(commandExecThreadCount);

	}

	/**
	 * @param cmd
	 * @return
	 */
	public CmdResult execute(final String[] cmdArray) {

		if (null == cmdArray || cmdArray.length < 1) {
			LOGGER.warn("cmdArray is empty!");
			return null;
		}
		try {
			final Process process = Runtime.getRuntime().exec(cmdArray);

			return callWithTimeout(new CallRunner<CmdResult>() {
				@Override
				public CmdResult call() throws Exception {

					CmdResult resut = new CmdResult(cmdArray);
					List<String> commonResList = new ArrayList<String>();
					List<String> errorResList = new ArrayList<String>();

					BufferedReader infoReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = null;
					try {
						while ((line = infoReader.readLine()) != null && !isInterrupted()) {
							System.out.println(line);
							commonResList.add(line);
						}
					} catch (Exception e) {
						System.err.println("time out");
					} finally {
						infoReader.close();
						System.err.println("close errorReader");
					}

					BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					try {
						while ((line = errorReader.readLine()) != null && !isInterrupted()) {
							System.out.println(line);
							errorResList.add(line);
						}
					} catch (Exception e) {
						System.err.println("time out");
					} finally {
						errorReader.close();
						System.err.println("close infoReader");
					}
					resut.setCommonResList(commonResList);
					resut.setErrorResList(errorResList);

					return resut;
				}
			});

		} catch (IOException e1) {
			LOGGER.error("IO error "+e1.getMessage(),e1);
		} catch (InterruptedException e2) {
			LOGGER.error("Interrupted error "+e2.getMessage(),e2);
		}

		return null;
	}

	public static <T> T callWithTimeout(final CallRunner<T> callRunner) throws IOException,
			InterruptedException {
		Future<T> future = callTimeoutPool.submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				return callRunner.call();
			}
		});
		try {
			if (callTimeout > 0) {
				return future.get(callTimeout, TimeUnit.MILLISECONDS);
			} else {
				return future.get();
			}
		} catch (TimeoutException eT) {
			future.cancel(true);

		} catch (ExecutionException e1) {
			Throwable cause = e1.getCause();
			if (cause instanceof IOException) {
				throw (IOException) cause;
			} else if (cause instanceof InterruptedException) {
				throw (InterruptedException) cause;
			} else if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				throw new RuntimeException(e1);
			}
		} catch (CancellationException ce) {
			throw new InterruptedException("Blocked callable interrupted");
		} catch (InterruptedException ex) {
			throw ex;
		} finally {
			callRunner.interrupt();
		}
		return null;
	}

	public abstract class CallRunner<T> {

		private boolean isInterrupted = false;

		abstract T call() throws Exception;

		boolean isInterrupted() {
			return isInterrupted;
		}

		void interrupt() {
			isInterrupted = true;
		}
	}

	public static void main(String[] args) {
		CmdOperateService inst = new CmdOperateService();
		inst.execute(new String[] { "/bin/bash", "-c", "ping -c 500 localhost" });
		System.out.println("end");
	}

}
