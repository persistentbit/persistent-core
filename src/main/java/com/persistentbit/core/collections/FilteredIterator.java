package com.persistentbit.core.collections;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * User: petermuys
 * Date: 7/07/16
 * Time: 17:33
 */
public class FilteredIterator<T> implements Iterator<T> {
    private boolean hasNext;
    private T next;
    private Predicate<T> filter;
    private Iterator<T> master;

    public FilteredIterator(Predicate<T> filter, Iterator<T> master) {
        this.filter = filter;
        this.master = master;
        doNext();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public T next() {
        T res = next;
        doNext();
        return res;
    }
    private void doNext(){

        do{
            hasNext = master.hasNext();
            if(hasNext == false){
                next = null;
                return;
            }
            next = master.next();
        }while(filter.test(next) == false);
    }


}