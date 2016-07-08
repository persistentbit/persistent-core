package com.persistentbit.core.collections;

import java.util.Iterator;

/**
 * User: petermuys
 * Date: 6/07/16
 * Time: 21:49
 */
public class PStreamAnd<T> implements PStream<T>{
    private final PStream<T> left;
    private final PStream<T> right;

    public PStreamAnd(PStream<T> left, PStream<T> right) {
        this.left = left;
        this.right = right;
    }


    @Override
    public Iterator<T> iterator() {

        Iterator<T> i1 = left.iterator();
        Iterator<T> i2 = right.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return i1.hasNext() || i2.hasNext();
            }

            @Override
            public T next() {
                if(i1.hasNext()){
                    return i1.next();
                }
                return i2.next();
            }
        };
    }

    @Override
    public PStream<T> reversed() {
        return new PStreamAnd<>(right.reversed(),left.reversed());
    }

    @Override
    public String toString() {
        return left.toString() + ".plusAll(" + right.toString() + ")";
    }
}
