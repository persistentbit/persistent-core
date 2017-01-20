package com.persistentbit.core.logging;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.logging.entries.LogContext;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.logging.entries.LogEntryException;
import com.persistentbit.core.logging.entries.LogEntryMessage;

/**
 * TODOC
 *
 * @author petermuys
 * @since 5/01/17
 */
public abstract class AbstractLogEntryLogging {

	protected final int stackEntryIndex;

	public AbstractLogEntryLogging(int stackEntryIndex) {
		this.stackEntryIndex = stackEntryIndex;
	}


	public abstract Nothing add(LogEntry logEntry);

	public <WL extends LoggedValue> WL add(WL withLogs){
		LogEntry le = withLogs.getLog();
		if(le.isEmpty()==false){
			add(le);
		}
		return withLogs;
	}


	public Nothing info(Object message){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.info, new LogContext(ste), objectToString(message)));
	}
	public Nothing info(String message, Object value){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.info, new LogContext(ste), objectToString(message) + ": " + objectToString(message)));
	}
	public Nothing info(String message, Object value,Object...otherValues){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		String values = objectToString(value) + ", " + objectsToString(otherValues);
		return add(LogEntryMessage.of(LogMessageLevel.info, new LogContext(ste), objectToString(message) + ": " + values));
	}
	public Nothing important(Object message){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.important, new LogContext(ste), objectToString(message)));
	}
	public Nothing important(String message, Object value){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.important, new LogContext(ste), objectToString(message) + ": " + objectToString(message)));
	}
	public Nothing important(String message, Object value,Object...otherValues){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		String values = objectToString(value) + ", " + objectsToString(otherValues);
		return add(LogEntryMessage.of(LogMessageLevel.important, new LogContext(ste), objectToString(message) + ": " + values));
	}
	public Nothing warning(Object message){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.warning, new LogContext(ste), objectToString(message)));
	}
	public Nothing warning(String message, Object value){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.warning, new LogContext(ste), objectToString(message) + ": " + objectToString(message)));
	}
	public Nothing error(Object message){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.error, new LogContext(ste), objectToString(message)));
	}
	public Nothing error(String message, Object value){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(LogEntryMessage.of(LogMessageLevel.error, new LogContext(ste), objectToString(message) + ": " + objectToString(message)));
	}

	public Nothing exception(Throwable cause) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[stackEntryIndex];
		return add(new LogEntryException(new LogContext(ste), cause));
	}




	public static String objectToString(Object message){
		if(message == null){
			return "null";
		}
		try{
			return message.toString();
		}catch(Exception e){
			return "<Message to string failed for class " + message.getClass() + ">";
		}
	}
	public static  final String objectsToString(Object...others){
		String res = "";
		for(Object param : others){
			if(res.isEmpty() == false){
				res += ", ";
			}
			res += objectToString(param);
		}
		return res;
	}


}
