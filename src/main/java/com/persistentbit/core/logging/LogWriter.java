package com.persistentbit.core.logging;

import com.persistentbit.core.utils.IndentOutputStream;
import com.persistentbit.core.utils.IndentPrintStream;

import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODOC
 *
 * @author petermuys
 * @since 16/12/16
 */
public class LogWriter{

	private boolean          hasError;

	private IndentPrintStream out;
	private       String                              indent              = "";
	private final Map<Integer, FunctionStartLogEntry> functionStartLookup = new HashMap<>();



	private final String timeStyle;
	private final String classStyle;
	private final String functionStyle;
	private final String functionParamsStyle;
	private final String functionResultStyle;
	private final String durationStyle;
	private final String msgStyleDebug;
	private final String msgStyleInfo;
	private final String msgStyleWarn;
	private final String msgStyleError;

	private LogWriter(boolean hasError, AnsiColor color,IndentPrintStream out) {

		this.hasError = hasError;
		this.out = out;

		this.timeStyle = color.faint().fgWhite().toString();
		this.classStyle = color.faint().fgWhite().toString();
		this.functionStyle = color.bold().fgYellow().toString();
		this.functionParamsStyle = color.fgYellow().toString();
		this.functionResultStyle  = color.fgBlue().toString();
		this.durationStyle = color.faint().fgWhite().toString();
		this.msgStyleDebug = color.fgCyan().toString();
		this.msgStyleInfo = color.fgGreen().toString();
		this.msgStyleWarn = color.fgRed().toString();
		this.msgStyleError = color.bold().fgRed().toString();
	}

	static public void write(boolean hasError, List<LogEntry> logEntries, PrintStream out) {
		LogWriter lw = new LogWriter(
				hasError,
				new AnsiColor(),
				new IndentPrintStream(new IndentOutputStream(out,".."))
		);
		//logEntries.forEach(le -> le.accept(lw));
		lw.write(logEntries);
	}

	public void write(List<LogEntry> entries) {
		int index = 0;
		while(index < entries.size()){
			LogEntry entry = entries.get(index);
			if(entry instanceof FunctionStartLogEntry){
				FunctionStartLogEntry fstart = (FunctionStartLogEntry) entry;
				int lastMessageIndex = findLastMessage(entries,fstart,index);
				logFunction(entries.subList(index,lastMessageIndex+1),fstart);
				index = lastMessageIndex+1;
			} else if(entry instanceof FunctionThrowsLogEntry){

				logException((FunctionThrowsLogEntry) entry);
				index++;
			} else if(entry instanceof MessageLogEntry){
				logMessage((MessageLogEntry)entry);
				index++;
			} else {
				out.println("Unknown:" + entry);
				index++;
			}
		}
	}

	private void logMessage(MessageLogEntry msg){
		String style = getMsgStyle(msg.getCategory());
		out.println(
				style +  msg.getMessage() +
						timeStyle + "\t… " + formatTime(msg.getTimestamp()) + " " +
						classStyle  +  msg.getClassName() + "(" + msg.getLineNumber() + ")"
		);
	}

	private void logException(FunctionThrowsLogEntry entry){
		out.print(msgStyleError);
		out.println(entry.getExceptionStackTrace());
	}

	private String getMsgStyle(LogCategory category){
		switch (category){
			case debug: return msgStyleDebug;
			case info: return msgStyleInfo;
			case warn: return msgStyleWarn;
			case error: return msgStyleError;
			default: throw new RuntimeException("Unknown category: " + category);
		}
	}

	private void logFunction(List<LogEntry> entries,FunctionStartLogEntry fstart){
		LogEntry last = entries.get(entries.size()-1);
		long duration = last.getTimestamp() -  fstart.getTimestamp();
		String durationStr = duration == 0 ? " " : " (" + duration + "ms) ";
		String params = fstart.getParams();
		params = params.isEmpty() ? params : "( " + params + " ) ";
		String returnValue = "";
		List<LogEntry> messages;
		if(last instanceof FunctionEndLogEntry){
			FunctionEndLogEntry fEnd = (FunctionEndLogEntry)last;
			returnValue = "\t= " + fEnd.getReturnValue();
			messages = entries.subList(1, entries.size()-1);
		} else{
			messages = entries.subList(1,entries.size());
		}


		out.println(
				functionStyle +  fstart.getMethodName() +
						functionParamsStyle + params +
						functionResultStyle + returnValue +
						timeStyle + "\t… " + formatTime(fstart.getTimestamp()) +
						durationStyle + durationStr  +
						classStyle  +  fstart.getClassName() + "(" + fstart.getLineNumber() + ")"
		);
		out.indent();
			write(messages);
		out.outdent();
	}

	private int findLastMessage(List<LogEntry> entries,FunctionStartLogEntry fstart, int index){
		int startLevel = fstart.getCallStackLength();
		for(int i = index+1; i<entries.size(); i++){
			LogEntry le = entries.get(i);
			if(le.getCallStackLength() < startLevel){
				//Call stack level < function start level -> must be after this call
				return i-1;
			}
			if(le instanceof FunctionEndLogEntry){
				FunctionEndLogEntry fele = (FunctionEndLogEntry)le;
				if(fele.getFunctionCallId() == fstart.getFunctionCallId()){
					return i;
				}
			}
		}
		return entries.size()-1;
	}

	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM hh:mm:ss.SSS");

	private String formatTime(long time) {
		return dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
	}
}
