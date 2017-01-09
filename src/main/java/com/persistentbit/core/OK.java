package com.persistentbit.core;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 9/01/2017
 */
public class OK {
    public static final OK inst = new OK();

    private OK() { }

    @Override
    public String toString() {
        return "OK";
    }
}
