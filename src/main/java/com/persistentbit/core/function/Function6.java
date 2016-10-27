package com.persistentbit.core.function;

/**
 * Functional Interface with 6 parameters
 *
 * @author Peter Muys
 */
@FunctionalInterface
public interface Function6<V1, V2, V3, V4, V5, V6, R>{

  R apply(V1 v1, V2 v2, V3 v3, V4 v4, V5 v5, V6 v6);
}
