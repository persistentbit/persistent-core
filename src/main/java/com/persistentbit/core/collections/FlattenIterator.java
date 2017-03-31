package com.persistentbit.core.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Peter Muys
 * @since 11/07/2016
 */
class FlattenIterator<T> implements Iterator<T>{

  private final Iterator<?>           parent;
  private       Iterator<? extends T> child;
  private       T                     next;

  public FlattenIterator(Iterator<?> core) {
	this.parent = core;
  }


  @Override
  public boolean hasNext() {
	this.getNext();
	return this.next != null;
  }

  @Override
  @SuppressWarnings("unchecked")
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

  @SuppressWarnings("unchecked")
  private void getNext() {
	if(this.next == null) {
	  if(this.child != null && this.child.hasNext()) {
		this.next = this.child.next();
	  }
	  else {
		if(this.parent.hasNext()) {
		  this.child = (Iterator<? extends T>) ((Iterable<?>) this.parent.next()).iterator();
		  this.getNext();
		}

	  }
	}
  }
}
