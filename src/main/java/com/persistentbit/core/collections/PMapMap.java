package com.persistentbit.core.collections;

import com.persistentbit.core.tuples.Tuple2;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * An immutable java {@link Map} backed by a {@link IPMap}
 * @author Peter Muys
 * @since 9/07/16
 */
class PMapMap<K, V> extends AbstractMap<K, V> implements PStreamable<Tuple2<K, V>>, Serializable{

	private final IPMap<K, V> master;

	PMapMap(IPMap<K, V> master) {
		this.master = master;
	}

	@Override
	public PStream<Tuple2<K, V>> pstream() {
		return master;
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
		return getOrDefault(key, null);
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

	@SuppressWarnings("unchecked")
	@Override
	public Set<Map.Entry<K, V>> entrySet() {

		return master.lazy().map(t -> (Map.Entry<K, V>) t).pset().toSet();
	}
}
