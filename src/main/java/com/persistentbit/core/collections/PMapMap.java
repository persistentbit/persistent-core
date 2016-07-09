package com.persistentbit.core.collections;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * User: petermuys
 * Date: 9/07/16
 * Time: 11:21
 */
public class PMapMap<K,V> extends AbstractMap<K,V> implements PStreamable,Serializable{
    private final PMap<K,V> master;

    public PMapMap(PMap<K, V> master) {
        this.master = master;
    }

    @Override
    public <T> PStream<T> asPStream() {
        return (PStream<T>)master;
    }

    @Override
    public int size() {
        return master.size();
    }

    @Override
    public boolean isEmpty() {
        return master.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return master.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return master.values().contains(value);
    }

    @Override
    public V get(Object key) {
        return getOrDefault(key,null);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        return master.keys().pset().toSet();
    }

    @Override
    public Collection<V> values() {
        return master.values().list();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {

        return master.lazy().map(t -> (Map.Entry<K,V>)t).pset().toSet();
    }
}
