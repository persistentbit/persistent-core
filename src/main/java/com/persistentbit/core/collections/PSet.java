package com.persistentbit.core.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * User: petermuys
 * Date: 8/07/16
 * Time: 15:33
 */
public class PSet<T> extends PStreamDirect<T,PSet<T>> implements Serializable{
    static public PSet<Object> sEmpty = new PSet<>();

    static public <T> PSet<T> empty() {
        return (PSet<T>)sEmpty;
    }

    private final PMap<T,T> map;

    public PSet() {
        this(PMap.empty());
    }

    static public PSet<Integer> forInt() {
        return empty();
    }
    static public PSet<Long> forLong() {
        return empty();
    }

    static public PSet<String> forString() {
        return empty();
    }
    static public PSet<Boolean> forBoolean() {
        return empty();
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
        return PStream.from(iter).with(this,(r, v)-> r.plus(v));
    }
    public PSet<T> plus(T value){
        return new PSet<>(map.put(value,value));
    }

    @Override
    public Iterator<T> iterator() {
        return map.keys().iterator();
    }

    public Set<T> toSet() {
        return new PSetSet<T>(this);
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
