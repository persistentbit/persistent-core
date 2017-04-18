package com.persistentbit.core.collections;

import com.persistentbit.core.doc.uml.UmlType;

import java.util.Iterator;
import java.util.Set;

/**
 * A {@link IPSet} persistent implementation.
 * @author Peter Muys
 * @since 8/07/16
 * @see IPSet
 */
@UmlType
public class PSet<T> extends AbstractPStreamDirect<T, PSet<T>> implements IPSet<T>{

	private static final PSet<Object> sEmpty = new PSet<>();
	private final PMap<T, T> map;

	public PSet() {
		this(PMap.empty());
	}

	PSet(PMap<T, T> map) {
		this.map = map;
	}

	public static PSet<Integer> forInt() {
		return empty();
	}

	@SuppressWarnings("unchecked")
	public static <T> PSet<T> empty() {
		return (PSet<T>) sEmpty;
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
	public static <T> PSet<T> val(T... elements) {
		PSet<T> res = PSet.empty();
		for(T v : elements) {
			res = res.plus(v);
		}
		return res;
	}

	@Override
	public PSet<T> plus(T value) {
		return new PSet<>(map.put(value, value));
	}

	@Override
	public PStream<T> lazy() {
		return new AbstractPStreamLazy<T>(){
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
	public Iterator<T> iterator() {
		return map.keys().iterator();
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
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof PSet == false) {
			return false;
		}
		PSet other = (PSet) o;
		return map.equals(other.map);
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}
}
