package com.persistentbit.core.function;

/**
 * Functional Interface with 3 parameters
 * @author Peter Muys
 */
@FunctionalInterface
public interface Function3<V1,V2,V3,R>  {
    R apply(V1 v1, V2 v2, V3 v3);
}
