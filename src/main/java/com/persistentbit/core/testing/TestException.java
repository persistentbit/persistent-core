package com.persistentbit.core.testing;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 6/01/2017
 */
public class TestException  extends RuntimeException{
    public TestException(String message, Throwable cause, int stackIndex){
        super(message,cause);
        if(stackIndex != 0){
            StackTraceElement[] st = getStackTrace();
            StackTraceElement[] newSt = new StackTraceElement[st.length-stackIndex];
            System.arraycopy(st,stackIndex,newSt,0,newSt.length);
            setStackTrace(newSt);
        }
    }
    public TestException(String message, Throwable cause){
        super(message,cause);
    }
    public TestException(String message){
        super(message);
    }
    public TestException(String message, int stackIndex){
        this(message,null,stackIndex);
    }
    public TestException(Throwable cause, int stackIndex){
        this(cause.getMessage(),cause,stackIndex);
    }
}
