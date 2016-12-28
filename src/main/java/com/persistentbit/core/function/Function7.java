package com.persistentbit.core.function;

import java.util.function.Function;

/**
 * Functional Interface with 7 parameters
 *
 * @author Peter Muys
 */
@FunctionalInterface
public interface Function7<V1, V2, V3, V4, V5, V6, V7, R>{

  R apply(V1 v1, V2 v2, V3 v3, V4 v4, V5 v5, V6 v6, V7 v7);

  default Function<V1, Function<V2, Function<V3, Function<V4, Function<V5, Function<V6, Function<V7, R>>>>>>> curry() {
    return v1 -> v2 -> v3 -> v4 -> v5 -> v6 -> v7 -> apply(v1, v2, v3, v4, v5, v6, v7);
  }

  static <A, B, C, D, E, F, G, R> Function<A, Function<B, Function<C, Function<D, Function<E, Function<F, Function<G, R>>>>>>> curry(
      Function7<A, B, C, D, E, F, G, R> f
  ) {
    return f.curry();
  }

}
