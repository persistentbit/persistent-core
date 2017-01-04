package com.persistentbit.core.logging;

import com.persistentbit.core.result.Result;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/01/17
 */
public class TestLog{

	public static void log(Function<FunctionLogging,Result<?>> testCode){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		LogContext lc = new LogContext(ste);
		LogEntryFunction lef = LogEntryFunction.of(lc);
		FunctionLogging fl = new FunctionLogging(lef,2);
		try{
			testCode.apply(fl).orElseThrow();
		}catch(Throwable e){
			LogPrinter lp = LogPrinter.consoleInColor();
			lp.print(fl.getLog());
			lp.print(e);
			throw e;
		}

	}

}
