package com.persistentbit.core.collections;

import java.util.Iterator;

/**
 * User: petermuys
 * Date: 6/07/16
 * Time: 20:30
 */
public abstract class PStreamLazy<T> implements PStream<T> {


    @Override
    public boolean equals(Object o){
        if(o == this){
            return true;
        }
        if(o instanceof PStream == false){
            return false;
        }
        PStream other =(PStream)o;
        Iterator i1 = iterator();
        Iterator i2 = iterator();
        while(i1.hasNext() && i2.hasNext()){
            Object v1 = i1.next();
            Object v2 = i2.next();
            return v1 ==null ? v2==null : v1.equals(v2);
        }
        return i1.hasNext() == i2.hasNext();
    }

    @Override
    public String toString() {
        return limit(100).toString("<",", ",">");
    }
}
