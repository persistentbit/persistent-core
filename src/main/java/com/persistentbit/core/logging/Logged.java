package com.persistentbit.core.logging;

import com.persistentbit.core.Result;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.utils.StringUtils;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class Logged{
	private LogEntry entry;

	public Logged(LogEntryFunction entry) {
		this.entry = entry;
	}

	@FunctionalInterface
	public interface LoggedCode<R>{
		R run(Logged l) throws Exception;
	}

	public LogEntry getLog(){
		return this.entry;
	}



	public void debug(String message){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		entry = entry.append(LogEntryMessage.of(new LogContext(ste), message));

	}
	public void debug(String message, Object...values){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		entry = entry.append(LogEntryMessage.of(new LogContext(ste), message + ":" + PStream.val(values).toString(",")));
	}

	public static Logged function(Object...params){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String strParams = PStream.val(params).toString(", ");
		return new Logged(new LogEntryFunction(new LogContext(ste), strParams, LogEntryGroup.empty()));
	}

	public <R> Result.Failure<R> fail(String message){
		return Result.failure(new LoggedException(message,this.getLog()));
	}
	public <R> Result.Failure<R> fail(String message,Throwable cause){
		return Result.failure(new LoggedException(message,cause,this.getLog()));
	}

	public <U extends LoggedValue> U add(U loggedValue){
		loggedValue.getLog().ifPresent(l -> {
			entry = entry.append((LogEntry)l);
		});
		return loggedValue;
	}
	public void add(LogEntry log){
		entry = entry.append(log);
	}
	public <U> Result<U> add(Result<U> result){
		result.getLog().ifPresent(l -> {
			add(l);
		});
		return result;
	}

	public <R> R log(LoggedCode<R> code){
		try{
			R result =  code.run(this);
			LogContext context = entry.getContext()
				.map(ctx -> ctx.withTimestamp(System.currentTimeMillis()))
				.orElse(null);
			String strResult = result == null ? "null" : StringUtils.present(result.toString(),40);
			entry = entry.append(new LogEntryResult(context, strResult));
			return result;
		}catch(Exception e){
			throw new LoggedException(e,this.entry);
		}
	}



	public <R> Result<R> logResult(LoggedCode<Result<R>> code){
		try{
			Result<R> result =  code.run(this);
			LogContext context = entry.getContext()
				.map(ctx -> ctx.withTimestamp(System.currentTimeMillis()))
				.orElse(null);
			String strResult = result == null ? "null" : StringUtils.present(result.toString(),40);
			entry = entry.append(new LogEntryResult(context, strResult));
			return result.withLog(entry);
		}catch(Exception e){
			throw new LoggedException(e,this.entry);
		}
	}


	/*public static <R> R decorate(LoggedCode<R> code){
		//StackTraceElement[] st = Thread.currentThread().getStackTrace();
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		Logged l = new Logged(new LogEntryFunction(new LogContext(ste)));
		try{
			return code.run(l);
		}catch(Exception e){
			throw new LoggedException(e,l.entry);
		}

	}*/
}
