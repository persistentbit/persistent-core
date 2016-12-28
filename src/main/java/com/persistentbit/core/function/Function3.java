package com.persistentbit.core.function;

import java.util.function.Function;

/**
 * Functional Interface with 3 parameters
 *
 * @author Peter Muys
 */
@FunctionalInterface
public interface Function3<V1, V2, V3, R>{

  R apply(V1 v1, V2 v2, V3 v3);

  default Function<V1, Function<V2, Function<V3, R>>> curry() {
    return v1 -> v2 -> v3 -> apply(v1, v2, v3);
  }

  static <A, B, C, R> Function<A, Function<B, Function<C, R>>> curry(Function3<A, B, C, R> f) {
    return f.curry();
  }

}
