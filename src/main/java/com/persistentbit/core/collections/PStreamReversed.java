package com.persistentbit.core.collections;

import java.util.Iterator;
import java.util.List;

/**
 * User: petermuys
 * Date: 6/07/16
 * Time: 21:32
 */
public class PStreamReversed<T> implements PStream<T>{

    private final PStream<T> master;
    private List<T> rev;
    public PStreamReversed(PStream<T> master){
        this.master = master;

    }


    @Override
    public synchronized Iterator<T> iterator() {
        if(master instanceof PStreamReversed){
            return ((PStreamReversed<T>)master).master.iterator();
        }
        if(rev == null){
            rev = master.list();
        }
        return new Iterator<T>() {
            int i = rev.size()-1;
            @Override
            public boolean hasNext() {
                return i>=0;
            }

            @Override
            public T next() {
                T res = rev.get(i);
                i--;
                return res;
            }
        };
    }

    @Override
    public String toString() {
        return "reversed(" + master + ")";
    }
}
