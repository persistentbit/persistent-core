package com.persistentbit.core.function;

/**
 * Functional Interface with 2 parameters
 *
 * @author Peter Muys
 */
@FunctionalInterface
public interface Function2<V1, V2, R>{

  R apply(V1 v1, V2 v2);
}
