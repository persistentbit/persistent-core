package com.persistentbit.core.function;

import java.util.function.Function;

/**
 * Functional Interface with 5 parameters
 *
 * @author Peter Muys
 */
@FunctionalInterface
public interface Function5<V1, V2, V3, V4, V5, R>{

  R apply(V1 v1, V2 v2, V3 v3, V4 v4, V5 v5);

  default Function<V1, Function<V2, Function<V3, Function<V4, Function<V5, R>>>>> curry() {
    return v1 -> v2 -> v3 -> v4 -> v5 -> apply(v1, v2, v3, v4, v5);
  }
}
