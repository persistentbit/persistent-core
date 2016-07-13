package com.persistentbit.core;

import java.util.function.Supplier;

/**
 * A Lazy Value Supplier.
 * When the lazy supplier is first called, then the value is retrieved from the provide Supplier.
 * Once it has a value, than the value will be reused in the next calls to get
 * User: petermuys
 * Date: 28/02/16
 * Time: 15:44
 */
public class Lazy<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T value;
    private boolean gotValue    =   false;

    /**
     * Init with the master supplier
     * @param supplier The master supplier
     */
    public Lazy(Supplier<T> supplier){
        this.supplier = supplier;
    }

    /**
     * Get the value from the master supplier on the first call.<br>
     * Then reuse the value in all the later calls.
     * @return The lazy value
     */
    public synchronized T get() {
        if(gotValue == false){
            value = supplier.get();
            gotValue = true;
        }
        return value;
    }
}
