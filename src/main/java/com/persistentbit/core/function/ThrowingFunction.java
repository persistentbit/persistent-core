package com.persistentbit.core.function;

/**
 * A Function that can throw an {@link Exception}
 *
 * @author petermuys
 * @since 14/01/17
 */
@FunctionalInterface
public interface ThrowingFunction<P, R, E extends Exception>{

	R apply(P value) throws E;
}
