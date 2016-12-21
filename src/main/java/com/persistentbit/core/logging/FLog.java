package com.persistentbit.core.logging;

import com.persistentbit.core.collections.PStream;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 16/12/2016
 */
public class FLog {
    private final LogCollector logCollector;
    private final int   functionCallId;

    public FLog(LogCollector logCollector, int functionCallId) {
        this.logCollector = logCollector;
        this.functionCallId = functionCallId;
    }



    public FLog warn(String message){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];
        logCollector.add(
                new MessageLogEntry(
                        Thread.currentThread().getId(),
                        functionCallId,
                        System.currentTimeMillis(),
                        currentElement.getClassName(),
                        currentElement.getMethodName(),
                        currentElement.getLineNumber(),
                        callStack.length,
                        LogCategory.warn,
                        message
                )
        );
        return this;
    }
    public <E extends Throwable> E error(E exception){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];
        logCollector.add(
                new MessageLogEntry(
                        Thread.currentThread().getId(),
                        functionCallId,
                        System.currentTimeMillis(),
                        currentElement.getClassName(),
                        currentElement.getMethodName(),
                        currentElement.getLineNumber(),
                        callStack.length,
                        LogCategory.error,
                        "Throwing " + exception.getClass().getSimpleName() + ": " + exception.getLocalizedMessage()
                )
        );
        return exception;
    }
    public FLog error(String message){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];
        logCollector.add(
                new MessageLogEntry(
                        Thread.currentThread().getId(),
                        functionCallId,
                        System.currentTimeMillis(),
                        currentElement.getClassName(),
                        currentElement.getMethodName(),
                        currentElement.getLineNumber(),
                        callStack.length,
                        LogCategory.error,
                        message
                )
        );
        return this;
    }
    public FLog debug(String message){
        Thread currentThread = Thread.currentThread();
        StackTraceElement[] callStack = currentThread.getStackTrace();
        StackTraceElement currentElement = callStack[2];
        logCollector.add(
                new MessageLogEntry(
                        currentThread.getId(),
                        functionCallId,
                        System.currentTimeMillis(),
                        currentElement.getClassName(),
                        currentElement.getMethodName(),
                        currentElement.getLineNumber(),
                        callStack.length,
                        LogCategory.debug,
                        message
                )
        );
        return this;
    }
    public FLog debug(String message, Object...values){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];
        message = message + ": " + PStream.from(values).toString(", ");
        logCollector.add(
                new MessageLogEntry(
                        Thread.currentThread().getId(),
                        functionCallId,
                        System.currentTimeMillis(),
                        currentElement.getClassName(),
                        currentElement.getMethodName(),
                        currentElement.getLineNumber(),
                        callStack.length,
                        LogCategory.debug,
                        message
                )
        );
        return this;
    }


    public FLog info(String message){
        Thread currentThread = Thread.currentThread();
        StackTraceElement[] callStack = currentThread.getStackTrace();
        StackTraceElement currentElement = callStack[2];
        logCollector.add(
                new MessageLogEntry(
                        currentThread.getId(),
                        functionCallId,
                        System.currentTimeMillis(),
                        currentElement.getClassName(),
                        currentElement.getMethodName(),
                        currentElement.getLineNumber(),
                        callStack.length,
                        LogCategory.info,
                        message
                )
        );
        return this;
    }
    public FLog info(String message, Object...values){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];
        message = message + ": " + PStream.from(values).toString(", ");
        logCollector.add(
                new MessageLogEntry(
                        Thread.currentThread().getId(),
                        functionCallId,
                        System.currentTimeMillis(),
                        currentElement.getClassName(),
                        currentElement.getMethodName(),
                        currentElement.getLineNumber(),
                        callStack.length,
                        LogCategory.info,
                        message
                )
        );
        return this;
    }
    public <R> R runl(Function<FLog, R> code){
        try{
            return code.apply(this);
        } catch (RuntimeException e){

            StackTraceElement[] callStack = e.getStackTrace();
            StackTraceElement currentElement = callStack[0];
            logCollector.add(
                    new FunctionThrowsLogEntry(
                            Thread.currentThread().getId(),
                            functionCallId,
                            System.currentTimeMillis(),
                            currentElement.getClassName(),
                            currentElement.getMethodName(),
                            currentElement.getLineNumber(),
                            callStack.length,
                            e.getLocalizedMessage(),
                            printStackTrace(e)
                    )
            );
            throw e;
        }
    }
    public <R> R run(Supplier<R> code){
        try{
            return code.get();
        } catch (RuntimeException e){

            StackTraceElement[] callStack = e.getStackTrace();
            StackTraceElement currentElement = callStack[0];
            logCollector.add(
                    new FunctionThrowsLogEntry(
                            Thread.currentThread().getId(),
                            functionCallId,
                            System.currentTimeMillis(),
                            currentElement.getClassName(),
                            currentElement.getMethodName(),
                            currentElement.getLineNumber(),
                            callStack.length,
                            e.getLocalizedMessage(),
                            printStackTrace(e)
                    )
            );
            throw e;
        }
    }
    private String printStackTrace(Throwable exception){
        StringWriter sw = new StringWriter();
        try(PrintWriter sout = new PrintWriter(sw)){
            exception.printStackTrace(sout);
        }
        return sw.toString();
    }


    public void thrown(Throwable exception){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];


        logCollector.add(
                new FunctionThrowsLogEntry(
                        Thread.currentThread().getId(),
                        functionCallId,
                        System.currentTimeMillis(),
                        currentElement.getClassName(),
                        currentElement.getMethodName(),
                        currentElement.getLineNumber(),
                        callStack.length,
                        exception.getLocalizedMessage(),
                        printStackTrace(exception)
                )
        );
    }

    public <R> R done(R returnValue){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];
        logCollector.add(
                new FunctionEndLogEntry(
                        Thread.currentThread().getId(),
                        functionCallId,
                        System.currentTimeMillis(),
                        currentElement.getClassName(),
                        currentElement.getMethodName(),
                        currentElement.getLineNumber(),
                        callStack.length,
                        returnValue == null ? null : returnValue.toString()
                )
        );
        return returnValue;
    }
    public void done(){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];
        logCollector.add(
                new FunctionEndLogEntry(
                        Thread.currentThread().getId(),
                        functionCallId,
                        System.currentTimeMillis(),
                        currentElement.getClassName(),
                        currentElement.getMethodName(),
                        currentElement.getLineNumber(),
                        callStack.length,
                        null
                )
        );
    }
}
