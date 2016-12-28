package com.persistentbit.core.experiments;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 27/12/16
 */
public interface Functor<T>{

	<R> Functor<R> map(Function<T, R> f);
}
