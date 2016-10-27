package com.persistentbit.core.collections;

import java.util.Iterator;
import java.util.Set;

/**
 * User: petermuys
 * Date: 8/07/16
 * Time: 15:33
 */
public class PSet<T> extends PStreamDirect<T,PSet<T>> implements IPSet<T>{
    private static final PSet<Object> sEmpty = new PSet<>();

    @SuppressWarnings("unchecked")
    public static <T> PSet<T> empty() {
        return (PSet<T>)sEmpty;
    }

    private final PMap<T,T> map;

    public PSet() {
        this(PMap.empty());
    }

    public static PSet<Integer> forInt() {
        return empty();
    }
    public static PSet<Long> forLong() {
        return empty();
    }

    public static PSet<String> forString() {
        return empty();
    }
    public static PSet<Boolean> forBoolean() {
        return empty();
    }
    @SafeVarargs
    public static <T> PSet<T> val(T...elements){
        PSet<T> res = PSet.empty();
        for(T v: elements){
            res = res.plus(v);
        }
        return res;
    }


    PSet(PMap<T,T> map){
        this.map = map;
    }


    @Override
    public PStream<T> lazy() {
        return new PStreamLazy<T>() {
            @Override
            public Iterator<T> iterator() {
                return PSet.this.iterator();
            }

            @Override
            public PSet<T> pset() {
                return PSet.this;
            }
        };

    }

    @Override
    protected PSet<T> toImpl(PStream<T> lazy) {
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

    @SuppressWarnings("unchecked")
	@Override
    public PSet<T> plusAll(Iterable<? extends T> iter) {
        return PStream.from(iter).with(this, PSet::plus);
    }
    public PSet<T> plus(T value){
        return new PSet<>(map.put(value,value));
    }

    @Override
    public Iterator<T> iterator() {
        return map.keys().iterator();
    }

    public Set<T> toSet() {
        return new PSetSet<>(this);
    }

    @Override
    public boolean contains(Object value) {
        return map.containsKey(value);
    }

    @Override
    public PSet<T> duplicates() {
        return PSet.empty();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(obj instanceof PSet == false){
            return false;
        }
        PSet other = (PSet)obj;
        return map.equals(other.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
