package com.persistentbit.core.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * An immutable java {@link Set} backed by an {@link IPSet}
 * @author Peter Muys
 * @since 9/07/16
 */
class PSetSet<T> implements Set<T>, PStreamable<T>, Serializable{

	private final IPSet<T> master;

	public PSetSet(IPSet<T> master) {
		this.master = master;
	}

	@Override
	public PStream<T> pstream() {
		return master;
	}

	@Override
	public boolean isEmpty() {
		return master.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return master.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return master.iterator();
	}

	@Override
	public Object[] toArray() {

		return master.toArray(Object.class);
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return master.toArray(a);
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

	public boolean equals(Object o) {
		if(o == this)
			return true;

		if(!(o instanceof Set))
			return false;
		Collection<?> c = (Collection<?>) o;
		if(c.size() != size())
			return false;
		try {
			return containsAll(c);
		} catch(ClassCastException | NullPointerException unused) {
			return false;
		}
	}

	@Override
	public int size() {
		return master.size();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return master.containsAll(c);
	}

	public int hashCode() {
		int h = 0;
		for(T obj : this) {
			if(obj != null)
				h += obj.hashCode();
		}
		return h;
	}

}
