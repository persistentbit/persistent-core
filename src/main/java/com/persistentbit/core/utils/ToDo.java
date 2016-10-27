package com.persistentbit.core.utils;

/**
 *@author petermuys
 *@since 26/09/16
 */
public class ToDo extends RuntimeException{
    public ToDo(String message) {
        super(message);
    }
    public ToDo(){
        this("Not Yet Implemented");
    }
}
