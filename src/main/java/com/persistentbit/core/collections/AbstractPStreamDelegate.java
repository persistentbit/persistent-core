package com.persistentbit.core.collections;

import com.persistentbit.core.tuples.Tuple2;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A PStream instance that delegates all functionality to a delegate PStream<br>
 * User: petermuys
 * Date: 20/07/16
 * Time: 17:56
 */
abstract class AbstractPStreamDelegate<T> implements PStream<T>{


  @Override
  public PStream<T> lazy() {
	return getDelegate().lazy();
  }

  protected abstract PStream<T> getDelegate();

  @Override
  public boolean isInfinite() {
	return getDelegate().isInfinite();
  }

  @Override
  public PStream<T> clear() {
	return getDelegate().clear();
  }

  @Override
  public PStream<T> limit(int count) {
	return getDelegate().limit(count);
  }

  @Override
  public PStream<T> dropLast() {
	return getDelegate().dropLast();
  }

  @Override
  public <R> PStream<R> map(Function<? super T, ? extends R> mapper) {
	return getDelegate().map(mapper);
  }

  @Override
  public PStream<T> filter(Predicate<? super T> p) {
	return getDelegate().filter(p);
  }

  @Override
  public PStream<T> filterNulls() {
	return getDelegate().filterNulls();
  }

  @Override
  public Optional<T> find(Predicate<? super T> p) {
	return getDelegate().find(p);
  }

  @Override
  public <Z> PStream<Tuple2<Z, T>> zip(PStream<Z> zipStream) {
	return getDelegate().zip(zipStream);
  }

  @Override
  public PStream<Tuple2<Integer, T>> zipWithIndex() {
	return getDelegate().zipWithIndex();
  }

  @Override
  public Stream<T> stream() {
	return getDelegate().stream();
  }

  @Override
  public PStream<T> sorted(Comparator<? super T> comp) {
	return getDelegate().sorted(comp);
  }

  @Override
  public PStream<T> sorted() {
	return getDelegate().sorted();
  }

  @Override
  public PStream<T> reversed() {
	return getDelegate().reversed();
  }

  @Override
  public PStream<T> plusAll(Iterable<? extends T> iter) {
	return getDelegate().plusAll(iter);
  }

  @Override
  public PStream<T> flattenPlusAll(Iterable<Iterable<? extends T>> iterIter) {
	return getDelegate().flattenPlusAll(iterIter);
  }

  @Override
  public boolean contains(Object value) {
	return getDelegate().contains(value);
  }

  @Override
  public boolean containsAll(Iterable<?> iter) {
	return getDelegate().containsAll(iter);
  }

  @Override
  public <K> PMap<K, PList<T>> groupBy(Function<? super T, ? extends K> keyGen) {
	return getDelegate().groupBy(keyGen);
  }

  @Override
  public <K> PMap<K, T> groupByOneValue(Function<? super T, ? extends K> keyGen) {
	return getDelegate().groupByOneValue(keyGen);
  }

  @Override
  public <K, V> PMap<K, PList<V>> groupBy(Function<? super T, ? extends K> keyGen,
										  Function<? super T, ? extends V> valGen
  ) {
	return getDelegate().groupBy(keyGen, valGen);
  }

  @Override
  public <K, V> PMap<K, V> groupByOneValue(Function<? super T, ? extends K> keyGen,
										   Function<? super T, ? extends V> valGen
  ) {
	return getDelegate().groupByOneValue(keyGen, valGen);
  }

  @Override
  public <K> POrderedMap<K, PList<T>> groupByOrdered(Function<? super T, ? extends K> keyGen) {
	return getDelegate().groupByOrdered(keyGen);
  }

  @Override
  public <K, V> POrderedMap<K, PList<V>> groupByOrdered(Function<? super T, ? extends K> keyGen,
														Function<? super T, ? extends V> valGen
  ) {
	return getDelegate().groupByOrdered(keyGen, valGen);
  }

  @Override
  public PStream<T> plus(T value) {
	return getDelegate().plus(value);
  }

  @Override
  public T fold(T init, BinaryOperator<T> binOp) {
	return getDelegate().fold(init, binOp);
  }

  @Override
  public <X> X with(X init, BiFunction<X, T, X> binOp) {
	return getDelegate().with(init, binOp);
  }

  @Override
  public Optional<T> headOpt() {
	return getDelegate().headOpt();
  }

  @Override
  public T head() {
	return getDelegate().head();
  }

  @Override
  public <X> PStream<X> cast(Class<X> itemClass) {
	return getDelegate().cast(itemClass);
  }

  @Override
  public Optional<T> lastOpt() {
	return getDelegate().lastOpt();
  }

  @Override
  public Optional<T> beforeLastOpt() {
	return getDelegate().beforeLastOpt();
  }

  @Override
  public PStream<T> replaceFirst(T original, T newOne) {
	return getDelegate().replaceFirst(original, newOne);
  }

  @Override
  public PStream<T> tail() {
	return getDelegate().tail();
  }

  @Override
  public Optional<T> max(Comparator<T> comp) {
	return getDelegate().max(comp);
  }

  @Override
  public Optional<T> min(Comparator<T> comp) {
	return getDelegate().min(comp);
  }

  @Override
  public Optional<T> min() {
	return getDelegate().min();
  }

  @Override
  public Optional<T> max() {
	return getDelegate().max();
  }

  @Override
  public boolean isEmpty() {
	return getDelegate().isEmpty();
  }

  @Override
  public int size() {
	return getDelegate().size();
  }

  @Override
  public int count(Predicate<? super T> predicate) {
	return getDelegate().count(predicate);
  }

  @SafeVarargs
  @Override
  public final PStream<T> plusAll(T v1, T... rest) {
	return getDelegate().plusAll(v1, rest);
  }

  @Override
  public T[] toArray() {
	return getDelegate().toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
	return getDelegate().toArray(a);
  }

  @Override
  public PList<T> plist() {
	return getDelegate().plist();
  }

  @Override
  public PSet<T> pset() {
	return getDelegate().pset();
  }

  @Override
  public POrderedSet<T> porderedset() {
	return getDelegate().porderedset();
  }

  @Override
  public PStream<T> distinct() {
	return getDelegate().distinct();
  }

  @Override
  public PStream<T> duplicates() {
	return getDelegate().duplicates();
  }

  @Override
  public PStream<Tuple2<HeadMiddleEnd, T>> headMiddleEnd() {
	return getDelegate().headMiddleEnd();
  }

  @Override
  public LList<T> llist() {
	return getDelegate().llist();
  }

  @Override
  public List<T> list() {
	return getDelegate().list();
  }

  @Override
  public List<T> toList() {
	return getDelegate().toList();
  }

  @Override
  public Optional<T> join(BinaryOperator<T> joiner) {
	return getDelegate().join(joiner);
  }

  @Override
  public <X> PStream<X> flatten() {
	return getDelegate().flatten();
  }

  @Override
  public PStream<String> mapString() {
	return getDelegate().mapString();
  }

  @Override
  public PStream<String> mapString(String nullValue) {
	return getDelegate().mapString(nullValue);
  }

  @Override
  public String toString(String sep) {
	return getDelegate().toString(sep);
  }

  @Override
  public String toString(String left, String sep, String right) {
	return getDelegate().toString(left, sep, right);
  }

  @Override
  public Iterator<T> iterator() {
	return getDelegate().iterator();
  }


  @Override
  public int hashCode() {
	return getDelegate().hashCode();
  }

  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public boolean equals(Object obj) {
	return getDelegate().equals(obj);
  }

  @Override
  public String toString() {
	return getDelegate().toString();
  }

  @Override
  public PStream<T> peek(Consumer<? super T> consumer) {
	return getDelegate().peek(consumer);
  }

  @Override
  public PStream<T> filterNotContainedIn(PStream<? extends T> others) {
	return getDelegate().filterNotContainedIn(others);
  }
}
