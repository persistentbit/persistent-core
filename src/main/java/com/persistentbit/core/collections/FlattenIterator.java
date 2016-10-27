package com.persistentbit.core.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Peter Muys
 * @since 11/07/2016
 */
public class FlattenIterator<T> implements Iterator<T>{

	private final Iterator<?> parent;
	private Iterator<? extends T> child = null;
	private T next;

	public FlattenIterator(Iterator<?> core) {
		this.parent = core;
	}


	public boolean hasNext() {
		this.getNext();
		return this.next != null;
	}

	public T next() {
		Object r = this.next;
		this.next = null;
		if(r == null) {
			throw new NoSuchElementException();
		}
		else {
			return (T) r;
		}
	}

	private void getNext() {
		if(this.next == null) {
			if(this.child != null && this.child.hasNext()) {
				this.next = this.child.next();
			}
			else {
				if(this.parent.hasNext()) {
					this.child = ((Iterable) this.parent.next()).iterator();
					this.getNext();
				}

			}
		}
	}
}
