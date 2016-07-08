package com.persistentbit.core.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * User: petermuys
 * Date: 8/07/16
 * Time: 15:33
 */
public class PSet<T> extends PStreamDirect<T,PSet<T>> {
    static public PSet<Object> sEmpty = new PSet<>();

    static public <T> PSet<T> empty() {
        return (PSet<T>)sEmpty;
    }

    private final PMap<T,T> map;

    public PSet() {
        this(PMap.empty());
    }

    private PSet(PMap<T,T> map){
        this.map = map;
    }


    @Override
    PSet<T> toImpl(PStream<T> lazy) {
        return lazy.pset();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public PSet<T> pset() {
        return this;
    }



    @Override
    public PSet<T> distinct() {
        return this;
    }

    @Override
    public PSet<T> plusAll(Iterable<T> iter) {
        return PStream.fromIter(iter).with(this,(r,v)-> r.plus(v));
    }
    public PSet<T> plus(T value){
        return new PSet<>(map.put(value,value));
    }

    @Override
    public Iterator<T> iterator() {
        return map.keys().iterator();
    }

    public Set<T> toSet() {
        return new Set<T>() {
            @Override
            public int size() {
                return PSet.this.size();
            }

            @Override
            public boolean isEmpty() {
                return PSet.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return PSet.this.contains(o);
            }

            @Override
            public Iterator<T> iterator() {
                return PSet.this.iterator();
            }

            @Override
            public Object[] toArray() {

                return PSet.this.toArray();
            }

            @Override
            public <T1> T1[] toArray(T1[] a) {
                int size = size();
                if(a.length<size){
                    a = Arrays.copyOf(a,size);
                }
                Iterator<T> iter = iterator();
                for(int t=0; t<a.length;t++){
                    if(iter.hasNext()){
                        a[t] = (T1)iter.next();
                    } else {
                        a[t] = null;
                    }
                }
                return a;
            }

            @Override
            public boolean add(T t) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return map.keys().containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends T> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
