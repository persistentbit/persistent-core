package com.persistentbit.core.collections;

import com.persistentbit.core.Tuple2;

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
public class PStreamDelegate<T> implements PStream<T>{

    private final Supplier<PStream<T>> delegate;


    /**
     * Create the PStream Delegated PStream
     * @param delegate The supplier of the delegate.
     */
    public PStreamDelegate(Supplier<PStream<T>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public PStream<T> lazy() {
        return delegate.get().lazy();
    }

    @Override
    public boolean isInfinit() {
        return delegate.get().isInfinit();
    }

    @Override
    public PStream<T> clear() {
        return delegate.get().clear();
    }

    @Override
    public PStream<T> limit(int count) {
        return delegate.get().limit(count);
    }

    @Override
    public PStream<T> dropLast() {
        return delegate.get().dropLast();
    }

    @Override
    public <R> PStream<R> map(Function<T, R> mapper) {
        return delegate.get().map(mapper);
    }

    @Override
    public PStream<T> filter(Predicate<T> p) {
        return delegate.get().filter(p);
    }

    @Override
    public Optional<T> find(Predicate<T> p) {
        return delegate.get().find(p);
    }

    @Override
    public <Z> PStream<Tuple2<Z, T>> zip(PStream<Z> zipStream) {
        return delegate.get().zip(zipStream);
    }

    @Override
    public PStream<Tuple2<Integer, T>> zipWithIndex() {
        return delegate.get().zipWithIndex();
    }

    @Override
    public Stream<T> stream() {
        return delegate.get().stream();
    }

    @Override
    public PStream<T> sorted(Comparator<? super T> comp) {
        return delegate.get().sorted(comp);
    }

    @Override
    public PStream<T> sorted() {
        return delegate.get().sorted();
    }

    @Override
    public PStream<T> reversed() {
        return delegate.get().reversed();
    }

    @Override
    public PStream<T> plusAll(Iterable<T> iter) {
        return delegate.get().plusAll(iter);
    }

    @Override
    public PStream<T> flattenPlusAll(Iterable<Iterable<T>> iterIter) {
        return delegate.get().flattenPlusAll(iterIter);
    }

    @Override
    public boolean contains(Object value) {
        return delegate.get().contains(value);
    }

    @Override
    public boolean containsAll(Iterable<?> iter) {
        return delegate.get().containsAll(iter);
    }

    @Override
    public <K> PMap<K, PList<T>> groupBy(Function<T, K> keyGen) {
        return delegate.get().groupBy(keyGen);
    }

    @Override
    public <K> PMap<K, T> groupByOneValue(Function<T, K> keyGen) {
        return delegate.get().groupByOneValue(keyGen);
    }

    @Override
    public <K, V> PMap<K, PList<V>> groupBy(Function<T, K> keyGen, Function<T, V> valGen) {
        return delegate.get().groupBy(keyGen, valGen);
    }

    @Override
    public <K, V> PMap<K, V> groupByOneValue(Function<T, K> keyGen, Function<T, V> valGen) {
        return delegate.get().groupByOneValue(keyGen, valGen);
    }

    @Override
    public <K> POrderedMap<K, PList<T>> groupByOrdered(Function<T, K> keyGen) {
        return delegate.get().groupByOrdered(keyGen);
    }

    @Override
    public <K, V> POrderedMap<K, PList<V>> groupByOrdered(Function<T, K> keyGen, Function<T, V> valGen) {
        return delegate.get().groupByOrdered(keyGen, valGen);
    }

    @Override
    public PStream<T> plus(T value) {
        return delegate.get().plus(value);
    }

    @Override
    public T fold(T init, BinaryOperator<T> binOp) {
        return delegate.get().fold(init,binOp);
    }

    @Override
    public <X> X with(X init, BiFunction<X, T, X> binOp) {
        return delegate.get().with(init,binOp);
    }

    @Override
    public Optional<T> headOpt() {
        return delegate.get().headOpt();
    }

    @Override
    public T head() {
        return delegate.get().head();
    }

    @Override
    public <X> PStream<X> cast(Class<X> itemClass) {
        return delegate.get().cast(itemClass);
    }

    @Override
    public Optional<T> lastOpt() {
        return delegate.get().lastOpt();
    }

    @Override
    public Optional<T> beforeLastOpt() {
        return delegate.get().beforeLastOpt();
    }

    @Override
    public PStream<T> replaceFirst(T original, T newOne) {
        return delegate.get().replaceFirst(original,newOne);
    }

    @Override
    public PStream<T> tail() {
        return delegate.get().tail();
    }

    @Override
    public Optional<T> max(Comparator<T> comp) {
        return delegate.get().max(comp);
    }

    @Override
    public Optional<T> min(Comparator<T> comp) {
        return delegate.get().min(comp);
    }

    @Override
    public Optional<T> min() {
        return delegate.get().min();
    }

    @Override
    public Optional<T> max() {
        return delegate.get().max();
    }

    @Override
    public boolean isEmpty() {
        return delegate.get().isEmpty();
    }

    @Override
    public int size() {
        return delegate.get().size();
    }

    @Override
    public int count(Predicate<T> predicate) {
        return delegate.get().count(predicate);
    }

    @Override
    public PStream<T> plusAll(T v1, T... rest) {
        return delegate.get().plusAll(v1,rest);
    }

    @Override
    public T[] toArray() {
        return delegate.get().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return delegate.get().toArray(a);
    }

    @Override
    public PList<T> plist() {
        return delegate.get().plist();
    }

    @Override
    public PSet<T> pset() {
        return delegate.get().pset();
    }

    @Override
    public POrderedSet<T> porderedset() {
        return delegate.get().porderedset();
    }

    @Override
    public PStream<T> distinct() {
        return delegate.get().distinct();
    }

    @Override
    public LList<T> llist() {
        return delegate.get().llist();
    }

    @Override
    public List<T> list() {
        return delegate.get().list();
    }

    @Override
    public List<T> toList() {
        return delegate.get().toList();
    }

    @Override
    public Optional<T> join(BinaryOperator<T> joiner) {
        return delegate.get().join(joiner);
    }

    @Override
    public <X> PStream<X> flatten() {
        return delegate.get().flatten();
    }

    @Override
    public PStream<String> mapString() {
        return delegate.get().mapString();
    }

    @Override
    public PStream<String> mapString(String nullValue) {
        return delegate.get().mapString(nullValue);
    }

    @Override
    public String toString(String sep) {
        return delegate.get().toString(sep);
    }

    @Override
    public String toString(String left, String sep, String right) {
        return delegate.get().toString(left,sep,right);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.get().iterator();
    }
}
