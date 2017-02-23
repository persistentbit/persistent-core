package com.persistentbit.core.easyscript;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 23/02/2017
 */
@FunctionalInterface
public interface ECallable {

	Object apply(Object... arguments);
}
