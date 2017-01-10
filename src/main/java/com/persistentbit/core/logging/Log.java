package com.persistentbit.core.logging;

import com.persistentbit.core.logging.entries.LogEntryFunction;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class Log{


	public static class FLogging extends FunctionLogging{
		public FLogging(LogEntryFunction lef) {
			super(lef,2);
		}

		public <R> R codeNoResultLog(FunctionLogging.LoggedFunction<R> code){
			try{
				R result = code.run(this);
				functionDoneTimestamp(System.currentTimeMillis());
				return result;
			}catch(Exception e){
				throw new LoggedException(e, getLog());
			}
		}
		@SuppressWarnings("unchecked")
		public  <R> R code(FunctionLogging.LoggedFunction<R> code){
			try{
				R result = code.run(this);
				functionDoneTimestamp(System.currentTimeMillis());
				functionResult(result);
				/*if(result instanceof LoggedValue){
					LoggedValue<?> lv = (LoggedValue)result;
					return (R) lv.mapLog(e -> {
						if(e.isEmpty() == false){
							return entry.append(e);
						}
						return entry;
					});
				}*/
				return result;
			}catch(Exception e){
				throw new LoggedException(e,entry);
			}
		}

	}

	public static FLogging function(){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		LogEntryFunction fe = LogEntryFunction.of(new LogContext(ste));
		return new FLogging(fe);
	}

	public static  FLogging function(Object...params){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		LogEntryFunction fe = LogEntryFunction.of(new LogContext(ste));
		FLogging res = new FLogging(fe);
		res.params(params);
		return res;
	}


}
