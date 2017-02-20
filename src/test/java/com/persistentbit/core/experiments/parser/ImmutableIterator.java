package com.persistentbit.core.experiments.parser;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 19/02/17
 */
public interface ImmutableIterator<T>{

	Optional<T> getOpt();

	ImmutableIterator<T> next();

	default T orElseThrow() {
		return getOpt().get();
	}

	default T orElse(T elseValue) {
		return getOpt().orElse(elseValue);
	}


}
