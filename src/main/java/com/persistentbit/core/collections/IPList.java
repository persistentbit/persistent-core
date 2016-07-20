package com.persistentbit.core.collections;

/**
 * User: petermuys
 * Date: 6/07/16
 * Time: 19:32
 */
public interface IPList<T> extends PStream<T>{
    T get(int index);
    IPList<T> put(int index, T value);

}
