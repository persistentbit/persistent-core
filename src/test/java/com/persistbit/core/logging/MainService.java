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
	private JpaPersoonDAO jpaPersoonDAO;

	public MainService(LogCollector logCollector) {
		this.logCollector = logCollector;
		this.jpaPersoonDAO = new JpaPersoonDAO(logCollector);
	}

	public Persoon getPersoon(int id){
		return logCollector.fun(id).run(() -> jpaPersoonDAO.getPersoon(id));
	}

	public int add(int a, int b) {
		FLog log = logCollector.fun(a, b);
		log.info("calculating a+b", a + b);
		return log.done(a + b);
	}

	public int multiply(int a, int b) {
		FLog log = logCollector.fun(a, b);
		log.debug("calculating a*b", a * b);
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
		return logCollector.fun(a,b).run(() -> {
			return doCalc(a,b);
		});
	}
	public int doCalc(int a, int b) {
		FLog log    = logCollector.fun(a, b);

		return log.done(doRealCalc(add(a,1),add(b,1)));
	}
	public int doRealCalc(int a, int b) {
		FLog log    = logCollector.fun(a, b);
		int  result = multiply(add(a, b), multiply(a, b));
		if(false) {
			log.error("Throwing test exception...");
			throw log.error(new RuntimeException("test"));
		}
		return log.done(result);
	}

	static public void main(String... args) {
		LogCollector  logFactory  = new LogCollector();
		MainService mainService = new MainService(logFactory);
		try {
			mainService.getPersoon(1234);
		}catch(Exception e){
			//e.printStackTrace();
		}
		mainService.calc(12, 34);
		mainService.div(1, 0);

		//System.out.println(logFactory);
		LogWriter.write(logFactory.hasError(), logFactory.getLogs().get(), System.out);
	}
}
