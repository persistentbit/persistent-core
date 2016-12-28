package com.persistentbit.core.function;


import java.util.function.Function;

/**
 * Functional Interface with 4 parameters
 *
 * @author Peter Muys
 */
@FunctionalInterface
public interface Function4<V1, V2, V3, V4, R>{

  R apply(V1 v1, V2 v2, V3 v3, V4 v4);

  default Function<V1, Function<V2, Function<V3, Function<V4, R>>>> curry() {
    return v1 -> v2 -> v3 -> v4 -> apply(v1, v2, v3, v4);
  }

  static <A, B, C, D, R> Function<A, Function<B, Function<C, Function<D, R>>>> curry(Function4<A, B, C, D, R> f) {
    return f.curry();
  }

}
