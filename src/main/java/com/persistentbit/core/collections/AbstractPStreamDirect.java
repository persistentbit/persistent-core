package com.persistentbit.core.collections;


import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Abstract base class for PStream implementations that implements the PStream methods not lazy.
 * @author Peter Muys
 * @since 7/07/16
 */
abstract class AbstractPStreamDirect<T, IMP extends PStream<T>> extends AbstractPStreamLazy<T>{

  @Override
  public IMP clear() {
	return toImpl(super.clear());
  }

  protected abstract IMP toImpl(PStream<T> lazy);

  @Override
  public IMP filter(Predicate<? super T> p) {
	return toImpl(super.filter(p));
  }

  @Override
  public PStream<T> filterNulls() {
	return toImpl(super.filterNulls());
  }

  @Override
  public Optional<T> find(Predicate<? super T> p) {
	return super.find(p);
  }

  @Override
  public PStream<T> sorted(Comparator<? super T> comp) {
	return lazy().sorted(comp);
  }

  @Override
  public PStream<T> lazy() {
	return new AbstractPStreamLazy<T>(){
	  @Override
	  public Iterator<T> iterator() {
		return AbstractPStreamDirect.this.iterator();
	  }

	};
  }

  @Override
  public Iterator<T> iterator() {
	return null;
  }

  @Override
  public PStream<T> sorted() {
	return lazy().sorted();
  }

  @Override
  public IMP reversed() {
	return toImpl(super.reversed());
  }

  @Override
  public IMP plusAll(Iterable<? extends T> iter) {
	return toImpl(super.plusAll(iter));
  }

  @Override
  public int hashCode() {
	int hashCode = 1;
	for(T e : this)
	  hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
	return hashCode;
  }


  @SuppressWarnings("AbstractMethodOverridesConcreteMethod")
  public abstract boolean equals(Object o);

  @Override
  public <R> PStream<R> map(Function<? super T, ? extends R> mapper) {
	return super.map(mapper);
  }

  @Override
  public IMP dropLast() {
	return toImpl(super.dropLast());
  }

  @Override
  public IMP plus(T value) {
	return toImpl(super.plus(value));
  }


  @Override
  public IMP plusAll(T v1, T... rest) {
	return toImpl(super.plusAll(v1, rest));
  }

  @Override
  public IMP distinct() {
	return toImpl(super.distinct());
  }

  @Override
  public IMP duplicates() {
	return toImpl(super.duplicates());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X> PStream<X> flatten() {
	return (PStream<X>) toImpl(super.flatten());
  }

  @Override
  public String toString() {
	return limit(100).toString(getClass().getSimpleName() + "[", ",", "]");
  }

  @Override
  public IMP limit(int count) {
	return toImpl(super.limit(count));
  }

  @Override
  public IMP replaceFirst(T original, T newOne) {
	return toImpl(super.replaceFirst(original, newOne));
  }
}
