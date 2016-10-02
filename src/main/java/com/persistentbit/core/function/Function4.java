package com.persistentbit.core.function;


/**
 * Functional Interface with 4 parameters
 * @author Peter Muys
 */
@FunctionalInterface
public interface Function4<V1,V2,V3,V4,R>  {
    R apply(V1 v1, V2 v2, V3 v3,V4 v4);
}
