package com.persistentbit.core.collections;

import java.io.Serializable;

/**
 * A Persistent Set
 * @author Peter Muys
 * @since 13/07/2016
 */
public interface IPSet<T> extends  PStream<T>,Serializable {
    @Override
    IPSet<T> plus(T value);

    @Override
    IPSet<T> plusAll(Iterable<T> iter) ;
}
