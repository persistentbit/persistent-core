package com.persistentbit.core.exceptions;

/**
 * Runnable code that can throw an exeception
 *
 * @author Peter Muys
 * @since 12/12/2016
 */
@FunctionalInterface
public interface TryCode<R> {
    R run() throws Throwable;
}
