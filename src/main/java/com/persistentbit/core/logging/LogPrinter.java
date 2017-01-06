package com.persistentbit.core.logging;

import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.IndentOutputStream;
import com.persistentbit.core.utils.IndentPrintStream;

import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class LogPrinter implements LogEntryPrinter{
	private IndentPrintStream out;

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
	private final String msgStyleException;

	private LogPrinter(AnsiColor color, IndentPrintStream out){
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
		this.msgStyleException = color.fgRed().toString();
	}




	public <R> R executeAndPrint(Callable<R> code){
		try{
			R result = code.call();
			if(result instanceof LoggedValue){
				print(((LoggedValue) result).getLog());
			}
			return result;
		}catch(Exception e){
			print(e);
			return null;
		}
	}

	private static LogEntryPrinter bufferedPrinter(
		OutputStream realOut,
		Function<IndentPrintStream,LogPrinter> printerSupplier
	){
		LogPrinterBuffered.RealPrinterSupplier bufferdSupplier = out -> charset ->
			IndentOutputStream.of(out)
				.flatMap(os-> IndentPrintStream.of(os, charset))
				.map(printerSupplier)
				.orElseThrow();
		return LogPrinterBuffered.buffered(realOut,bufferdSupplier).orElseThrow();
	}


	public static LogEntryPrinter consoleInColor(){
		return bufferedPrinter(System.out,ips -> new LogPrinter(new AnsiColor(true),ips));
	}
	public static LogEntryPrinter consoleErrorInColor(){
		return bufferedPrinter(System.err,ips -> new LogPrinter(new AnsiColor(true),ips));
	}

	/*public static LogEntryPrinter memory(ByteArrayOutputStream bout){
		return IndentOutputStream.of(bout)
				.flatMap(os -> IndentPrintStream.of(os,Charset.forName("UTF-8")))
				.map(s -> new LogPrinter(new AnsiColor(false),s))
				.orElseThrow();
	}*/






	public void print(LogEntry entry) {
		switch(entry.getClass().getSimpleName()){
			case "LogEntryFunction":	print((LogEntryFunction)entry); break;
			case "LogEntryGroup" : print((LogEntryGroup)entry);break;
			case "LogEntryMessage": print((LogEntryMessage)entry);break;
			case "LogEntryException": print((LogEntryException) entry); break;
			default:
				out.println(entry);break;
		}
	}

	public void print(Throwable exception){
		if(exception instanceof LoggedException){
			LoggedException le = (LoggedException) exception;
			String msg = le.getMessage() == null ? "" : le.getMessage();
			out.println(msgStyleException + "Logged Exception: " + msg);
			out.indent();
			print(le.getLogs());
			print(exception.getStackTrace());
			//out.indent();
			//out.outdent();

			//out.outdent();
			//out.outdent();
			/*out.indent();
			print(exception.getStackTrace());
			out.outdent();
			if(exception.getCause() != null){
				out.println(msgStyleException + " caused by..");
				out.indent();
				print(exception.getCause());
				out.outdent();
			}*/

		} else {
			out.println(msgStyleException + exception.getMessage() + msgStyleInfo + " " + exception.getClass().getName());
			out.indent();
			print(exception.getStackTrace());
			out.outdent();

		}
		if(exception.getCause() != null){
			out.println(msgStyleException + " caused by..");
			out.indent();
			print(exception.getCause());
			out.outdent();
		}

	}

	private void print(StackTraceElement[] stackTraceElements){
		for(StackTraceElement element : stackTraceElements){
			out.println(classStyle + element.getClassName() + "." + element.getMethodName() + "(" + element.getFileName()+":" + element.getLineNumber()+")");
		}

	}

	private void print(LogEntryFunction entry){
		print(null,entry);
	}

	private void print(Throwable exception,LogEntryFunction entry){

		String functionName = entry.getContext().map(s -> {
			String fun = s.getMethodName();
			String clsName = s.getClassName();
			int i = clsName.lastIndexOf('.');
			if(i >=0){
				clsName = clsName.substring(i+1);
			}
			return clsName.replace('$','.') + "." + fun;
		}).orElse("unknownFunction");


		String duration = entry.getTimestampDone().map( td ->
					entry.getContext().map(c ->
						   " " + (td - c.getTimestamp()) + "ms "
					).orElse("")
		).orElse("");
		String returnValue = entry.getResult().map(r -> ": " + r).orElse("");
		out.println(
			functionStyle +  functionName +
				functionParamsStyle + entry.getParams().map(p -> "(" + p + ")").orElse("(?)") +
				functionResultStyle + returnValue +
				timeStyle + "\t… " + entry.getContext().map(s -> formatTime(s.getTimestamp()) + " ").orElse("") +
				durationStyle + duration  +
				classStyle  +  entry.getContext().map(s -> s.getClassName() + "(" + s.getFileName() + ":" + s.getSourceLine() + ")").orElse("")
		);
		out.indent();
		print(entry.getLogs());
		//entry.getLogs().getEntries().forEach(le -> print(le));
		out.indent();
		if(exception != null) { print(exception.getStackTrace()); }
		out.outdent();
		out.outdent();
		//if(exception.getMessage() != null){
			//out.println(msgStyleException + exception.getMessage());
		//}
		//out.outdent();
		//out.outdent();
		/*
		print(exception.getStackTrace());
		out.outdent();
		if(exception.getCause() != null){
			out.println(msgStyleException + " caused by..");
			out.indent();
			print(exception.getCause());
			out.outdent();
		}
		out.outdent();*/
	}
	private void print(LogEntryGroup entry){
		entry.getEntries().forEach(e -> print(e));
	}
	private void print(LogEntryMessage entry){
		//out.println(entry.getMessage() + "___" + entry.getSource());
		out.println(
			msgStyleDebug +  entry.getMessage() +

				timeStyle + "\t… " + entry.getContext().map(s -> formatTime(s.getTimestamp()) + " ").orElse("") +
				classStyle  +  entry.getContext().map(s -> s.getClassName() + "(" + s.getFileName() + ":" + s.getSourceLine() + ")").orElse("")
		);
	}
	private void print(LogEntryException entry){
		out.println(
			msgStyleError +  entry.getCause().getMessage() +
				timeStyle + "\t… " + entry.getContext().map(s -> formatTime(s.getTimestamp()) + " ").orElse("") +
				classStyle  +  entry.getContext().map(s -> s.getClassName() + "(" + s.getFileName() + ":" + s.getSourceLine() + ")").orElse("")
		);
		print(entry.getCause());
		//out.print(msgStyleException);
		//entry.getCause().printStackTrace(out);
		//print(entry.getCause().getStackTrace());
		//out.println();
		//out.flush();
	}

	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM hh:mm:ss.SSS");

	private String formatTime(long time) {
		return dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
	}


	static void testCode(Function<FunctionLogging,Result<?>> testCode){
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		LogContext lc = new LogContext(ste);
		LogEntryFunction lef = LogEntryFunction.of(lc);
		FunctionLogging fl = new FunctionLogging(lef,2);
		try{
			testCode.apply(fl).orElseThrow();
		}catch(Throwable e){
			LogEntryPrinter lp = LogPrinter.consoleInColor();
			lp.print(fl.getLog());
			lp.print(e);
			throw e;
		}

	}
}
