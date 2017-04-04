package com.persistentbit.core.collections;

import com.persistentbit.core.doc.Support;

import java.io.Serializable;

/**
 * A Persistent Set.
 *
 * @author Peter Muys
 * @since 13/07/2016
 * @see PSet
 * @see POrderedSet
 * @see PStream
 */
@Support
public interface IPSet<T> extends PStream<T>, Serializable{

  @Override
  IPSet<T> plus(T value);

  @Override
  IPSet<T> plusAll(Iterable<? extends T> iter);
}
