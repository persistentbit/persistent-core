package com.persistentbit.core.function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 14/01/17
 */
@FunctionalInterface
public interface ThrowingFunction<P, R, E extends Exception>{

	R apply(P value) throws E;
}
