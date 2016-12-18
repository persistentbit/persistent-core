package com.persistbit.core.logging;

import com.persistentbit.core.logging.FLog;
import com.persistentbit.core.logging.LogCollector;
import com.persistentbit.core.logging.LogWriter;

/**
 * TODOC
 *
 * @author petermuys
 * @since 13/12/16
 */
public class MainService{

	private LogCollector logCollector;

	public MainService(LogCollector logCollector) {
		this.logCollector = logCollector;
	}

	public int add(int a, int b) {
		FLog log = logCollector.fun(a, b);
		log.debug("calculating a+b = ", a + b);
		return log.done(a + b);
	}

	public int multiply(int a, int b) {
		FLog log = logCollector.fun(a, b);
		log.debug("calculating a*b = ", a * b);
		return log.done(a * b);
	}

	public int div(int a, int b) {
		FLog log = logCollector.fun(a, b);
		if(b == 0) {
			log.warn("divide by 0");
			return log.done(0);
		}
		log.debug("calculating a/b = ", a / b);
		return log.done(a / b);
	}

	public int calc(int a, int b) {
		FLog log    = logCollector.fun(a, b);
		int  result = multiply(add(a, b), multiply(a, b));
		return log.done(result);
	}


	static public void main(String... args) {
		LogCollector  logFactory  = new LogCollector();
		MainService mainService = new MainService(logFactory);
		mainService.calc(12, 34);
		//System.out.println(logFactory);
		LogWriter.write(logFactory.hasError(), logFactory.getLogs().get(), s -> System.out.println(s));
	}
}
