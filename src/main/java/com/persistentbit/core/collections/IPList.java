package com.persistentbit.core.collections;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * User: petermuys
 * Date: 6/07/16
 * Time: 19:32
 */
public interface IPList<T> extends PStream<T>{

  T get(int index);
  default Optional<T> getOpt(int index){
      return index >=0 && index < size()
              ? Optional.ofNullable(get(index))
              : Optional.empty();
  }

  IPList<T> put(int index, T value);

  <R> R match(
	  Supplier<R> emptyList,
	  Function<T,R>  singleton,
	  Function<IPList<T>, R> multiple
  );
}
