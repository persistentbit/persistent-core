package com.persistentbit.core.exceptions;

/**
 * Unchecked Wrapper exception.<br>
 *
 * @author Peter Muys
 * @since 12/12/2016
 * @see Try
 */
public class TryFailureException extends RuntimeException{
    public TryFailureException() {
    }

    public TryFailureException(String message) {
        super(message);
    }

    public TryFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public TryFailureException(Throwable cause) {
        super(cause);
    }

    public TryFailureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
