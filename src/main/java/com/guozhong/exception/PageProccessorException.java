package com.guozhong.exception;
/**
 * 当然任务发送不确定性异常时抛出
 * @author Administrator
 *
 */
public final class PageProccessorException extends RuntimeException {

	public PageProccessorException() {
		// TODO Auto-generated constructor stub
	}

	public PageProccessorException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public PageProccessorException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public PageProccessorException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public PageProccessorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
