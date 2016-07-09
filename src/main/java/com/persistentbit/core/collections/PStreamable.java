package com.persistentbit.core.collections;

/**
 * Marks a class as an instance of PStream.<br>
 * The PStream.from(...) checks for this interface
 * User: petermuys
 * Date: 9/07/16
 * Time: 09:36
 */
public interface PStreamable {
    <T> PStream<T>   asPStream();
}
