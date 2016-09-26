package com.persistentbit.core.utils;

/**
 * Created by petermuys on 26/09/16.
 */
public class NotYet extends RuntimeException{
    public NotYet(String message) {
        super(message);
    }
    public NotYet(){
        this("Not Yet Implemented");
    }
}
