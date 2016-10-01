package com.persistentbit.core.utils.functional;

/**
 * Created by petermuys on 1/10/16.
 */
@FunctionalInterface
public interface Function3<V1,V2,V3,R>  {
    R apply(V1 v1, V2 v2, V3 v3);
}
