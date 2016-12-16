package com.persistentbit.core.logging;

import com.persistentbit.core.collections.PStream;

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

    public FLog debug(String message){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];
        logCollector.add(
                new MessageLogEntry(
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
    public FLog warn(String message){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];
        logCollector.add(
                new MessageLogEntry(
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
    public FLog debug(String message, Object...values){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];
        message = message + ": " + PStream.from(values).toString(", ");
        logCollector.add(
                new MessageLogEntry(
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

    public <R> R run(Supplier<R> code){
        try{
            return code.get();
        } catch (RuntimeException e){

            StackTraceElement[] callStack = e.getStackTrace();
            StackTraceElement currentElement = callStack[0];
            logCollector.add(
                    new FunctionThrowsLogEntry(
                            functionCallId,
                            System.currentTimeMillis(),
                            currentElement.getClassName(),
                            currentElement.getMethodName(),
                            currentElement.getLineNumber(),
                            callStack.length,
                            e.getLocalizedMessage(),
                            "TODO exceptionStackTrace"
                    )
            );
            throw e;
        }
    }

    public <R> R done(R returnValue){
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        StackTraceElement currentElement = callStack[2];
        logCollector.add(
                new FunctionEndLogEntry(
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
