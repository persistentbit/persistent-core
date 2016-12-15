package com.persistbit.core.logging;

import com.persistentbit.core.logging.FLog;
import com.persistentbit.core.logging.LogFactory;

/**
 * TODOC
 *
 * @author petermuys
 * @since 13/12/16
 */
public class MainService{

	private LogFactory logFactory;

	public MainService(LogFactory logFactory) {
		this.logFactory = logFactory;
	}

	public int add(int a, int b) {
		FLog log = logFactory.flog(a, b);
		log.debug("calculating a+b = ", a + b);
		return log.result(a + b);
	}

	public int multiply(int a, int b) {
		FLog log = logFactory.flog(a, b);
		log.debug("calculating a*b = ", a * b);
		return log.result(a * b);
	}

	public int div(int a, int b) {
		FLog log = logFactory.flog(a, b);
		if(b == 0) {
			log.warn("divide by 0");
			return log.result(0);
		}
		log.debug("calculating a/b = ", a / b);
		return log.result(a / b);
	}

	public int calc(int a, int b) {
		FLog log    = logFactory.flog(a, b);
		int  result = multiply(add(a, b), multiply(a, b));
		return log.result(result);
	}


	static public void main(String... args) {
		LogFactory  logFactory  = new LogFactory();
		MainService mainService = new MainService(logFactory);
		mainService.calc(12, 34);
		System.out.println(logFactory);
	}
}
