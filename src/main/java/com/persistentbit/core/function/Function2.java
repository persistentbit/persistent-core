package com.persistentbit.core.function;

import java.util.function.Function;

/**
 * Functional Interface with 2 parameters
 *
 * @author Peter Muys
 */
@FunctionalInterface
public interface Function2<V1, V2, R>{

  R apply(V1 v1, V2 v2);

  default Function<V1, Function<V2, R>> curry() {
    return v1 -> v2 -> apply(v1, v2);
  }
}
