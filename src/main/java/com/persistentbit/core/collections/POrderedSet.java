package com.persistentbit.core.collections;

import com.persistentbit.core.doc.uml.UmlType;

import java.util.Iterator;
import java.util.Set;

/**
 * A Persistent Set where the order of adding elements is preserved when iterating
 *
 * @author Peter Muys
 * @see IPSet
 * @since 13/07/2016
 */
@UmlType
public final class POrderedSet<T> extends AbstractPStreamDirect<T, POrderedSet<T>> implements IPSet<T>{

	private static final POrderedSet<Object> sEmpty = new POrderedSet<>();
	private final POrderedMap<T, T> map;

	public POrderedSet() {
		this(POrderedMap.empty());
	}

	private POrderedSet(POrderedMap<T, T> map) {
		this.map = map;
	}

	public static POrderedSet<Integer> forInt() {
		return empty();
	}

	@SuppressWarnings("unchecked")
	public static <T> POrderedSet<T> empty() {
		return (POrderedSet<T>) sEmpty;
	}

	@SafeVarargs
	public static <T> POrderedSet<T> val(T... elements) {
		POrderedSet<T> res = POrderedSet.empty();
		for(T v : elements) {
			res = res.plus(v);
		}
		return res;
	}


	public static POrderedSet<Long> forLong() {
		return empty();
	}

	public static POrderedSet<String> forString() {
		return empty();
	}

	public static POrderedSet<Boolean> forBoolean() {
		return empty();
	}

	@Override
	public PStream<T> lazy() {
		return new AbstractPStreamLazy<T>(){
			@Override
			public Iterator<T> iterator() {
				return POrderedSet.this.iterator();
			}

			@Override
			public POrderedSet<T> porderedset() {
				return POrderedSet.this;
			}

			@Override
			public PSet<T> pset() {
				return new PSet<>(map.pmap());
			}
		};

	}

	@Override
	public Iterator<T> iterator() {
		return map.keys().iterator();
	}

	@Override
	protected POrderedSet<T> toImpl(PStream<T> lazy) {
		return lazy.porderedset();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public PSet<T> pset() {
		return new PSet<>(map.pmap());
	}

	@Override
	public POrderedSet<T> porderedset() {
		return this;
	}

	@Override
	public POrderedSet<T> distinct() {
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public POrderedSet<T> plusAll(Iterable<? extends T> iter) {
		return PStream.from(iter).with(this, POrderedSet::plus);
	}

	@Override
	public POrderedSet<T> plus(T value) {
		return new POrderedSet<>(map.put(value, value));
	}

	@Override
	public boolean contains(Object value) {
		return map.containsKey(value);
	}

	public Set<T> toSet() {
		return new PSetSet<>(this);
	}

	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof IPSet == false) {
			return false;
		}
		IPSet other = (IPSet) o;
		if(this.size() != other.size()) {
			return false;
		}
		for(T v : this) {
			if(other.contains(v) == false) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}
}
