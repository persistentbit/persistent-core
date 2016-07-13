package com.persistentbit.core.collections;



import com.persistentbit.core.Tuple2;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * User: petermuys
 * Date: 7/07/16
 * Time: 17:25
 */
public abstract class PStreamDirect<T,IMP extends PStream<T>> extends PStreamLazy<T>{

    abstract protected IMP toImpl(PStream<T> lazy);

    @Override
    public PStream<T> lazy() {
        return new PStreamLazy<T>() {
            @Override
            public Iterator<T> iterator() {
                return PStreamDirect.this.iterator();
            }

        };
    }

    @Override
    public IMP clear() {
        return toImpl(super.clear());
    }

    @Override
    public IMP limit(int count) {
        return toImpl(super.limit(count));
    }


    @Override
    public IMP filter(Predicate<T> p) {
        return toImpl(super.filter(p));
    }

    @Override
    public Optional<T> find(Predicate<T> p) {
        return super.find(p);
    }


    @Override
    public PStream<Tuple2<Integer, T>> zipWithIndex() {
        return super.zipWithIndex();
    }


    @Override
    public PStream<T> sorted(Comparator<? super T> comp) {
        return lazy().sorted(comp);
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
    public IMP plusAll(Iterable<T> iter) {
        return toImpl(super.plusAll(iter));
    }
    @Override
    public int hashCode() {
        int hashCode = 1;
        for (T e : this)
            hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
        return hashCode;
    }


    public abstract boolean equals(Object obj);

    @Override
    public <R> PStream<R> map(Function<T, R> mapper) {
        return super.map(mapper);
    }

    @Override
    public <Z> PStream<Tuple2<Z, T>> zip(PStream<Z> zipStream) {
        return super.zip(zipStream);
    }

    @Override
    public Stream<T> stream() {
        return super.stream();
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
    public IMP plusAll(T val1, T... rest) {
        return toImpl(super.plusAll(val1,rest));
    }

    @Override
    public IMP distinct() {
        return toImpl(super.distinct());
    }

    @Override
    public <X> PStream<X> flatten() {
        return (PStream<X>) toImpl(super.flatten());
    }

    @Override
    public String toString() {
        return limit(100).toString(getClass().getSimpleName()  + "[" ,"," , "]");
    }


}
