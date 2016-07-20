package com.persistentbit.core.collections;


import com.persistentbit.core.Tuple2;


import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Peter Muys
 * @since 6/07/2016
 */
public interface PStream<T> extends Iterable<T> {

    /**
     * Create a PStream from an Optional value.
     * @param opt The optional
     * @param <T> The type of the PStream
     * @return An empty PStream or a Pstream with 1 element
     */
    static <T> PStream<T> from(Optional<T> opt){
        if(opt.isPresent()){
            return val(opt.get());
        }
        return val();
    }

    /**
     * Create a PStream from an {@link Iterator} or a {@link PStreamable}<br>
     * @param iter The Iterator or PStreamable
     * @param <T> The type of the resulting stream
     * @return The PStream
     */
    static <T> PStream<T> from(Iterable<T> iter){
        if(iter instanceof PStream){
            return ((PStream<T>)iter);
        }
        if(iter instanceof PStreamable){
            return ((PStreamable<T>)iter).pstream().lazy();
        }
        if(iter instanceof Collection){
            Collection col = (Collection)iter;
            Object[] arr = col.toArray();
            return new PStreamLazy<T>(){
                @Override
                public Iterator<T> iterator() {
                    return new Iterator<T>(){
                        int i = 0;
                        @Override
                        public boolean hasNext() {
                            return i< arr.length;
                        }

                        @Override
                        public T next() {
                            return (T)arr[i++];
                        }
                    };
                }
            };
        }
        return PList.<T>empty().plusAll(iter).lazy();
    }
    static <T> PStream<T> from(T[] values){
        if(values == null){
            return PList.<T>empty().lazy();
        }

        Object[] fixed = new Object[values.length];
        System.arraycopy(values,0,fixed,0,values.length);
        return new PStreamLazy<T>(){
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>(){
                    int i = 0;
                    @Override
                    public boolean hasNext() {
                        return i< fixed.length;
                    }

                    @Override
                    public T next() {
                        return (T)fixed[i++];
                    }
                };
            }
        };
    }

    static <T> PStream<T> val(T...values){
        return new PStreamLazy<T>(){
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>(){
                    int i = 0;
                    @Override
                    public boolean hasNext() {
                        return i< values.length;
                    }

                    @Override
                    public T next() {
                        return values[i++];
                    }
                };
            }
        };
    }

    static <T> PStream<T> sequence(T start, Function<T, T> next){
        return new PStreamLazy<T>() {

            @Override
            public boolean isInfinit() {
                return true;
            }

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    T v = start;
                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public T next() {
                        T res = v;
                        v = next.apply(v);
                        return res;
                    }
                };
            }


        };
    }

    static PStream<Integer> sequence(int start){
        return sequence(start,i -> i+1);
    }
    static PStream<Long> sequence(long start){
        return sequence(start,i -> i+1);
    }

    static <T> PStream<T> repeatValue(T value){
        return new PStreamLazy<T>() {
            @Override
            public boolean isInfinit() {
                return true;
            }

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public T next() {
                        return value;
                    }
                };
            }


        };
    }
    static <E> E[] newArray(int length, E... array) { return Arrays.copyOf(array, length); }



    PStream<T> lazy();




    boolean isInfinit();




    PStream<T> clear();



    PStream<T> limit(int count);
    PStream<T>  dropLast();
    <R> PStream<R> map(Function<T, R> mapper);
    PStream<T> filter(Predicate<T> p);

    Optional<T> find(Predicate<T> p);

    <Z> PStream<Tuple2<Z,T>> zip(PStream<Z> zipStream);
    PStream<Tuple2<Integer,T>> zipWithIndex();
    Stream<T> stream();
    PStream<T> sorted(Comparator<? super T> comp);
    PStream<T> sorted();
    PStream<T> reversed();
    PStream<T> plusAll(Iterable<T> iter);
    PStream<T> flattenPlusAll(Iterable<Iterable<T>> iterIter);

    boolean contains(Object value);

    boolean containsAll(Iterable<?> iter);


    <K> PMap<K,PList<T>> groupBy(Function<T, K> keyGen);
    <K> PMap<K,T> groupByOneValue(Function<T, K> keyGen);
    <K,V> PMap<K,PList<V>> groupBy(Function<T, K> keyGen,Function<T,V> valGen);
    <K,V> PMap<K,V> groupByOneValue(Function<T, K> keyGen,Function<T,V> valGen);

    <K> POrderedMap<K,PList<T>> groupByOrdered(Function<T, K> keyGen);
    <K,V> POrderedMap<K,PList<V>> groupByOrdered(Function<T, K> keyGen,Function<T,V> valGen);
    PStream<T> plus(T value);
    T fold(T init, BinaryOperator<T> binOp);

    <X> X with(X init, BiFunction<X, T, X> binOp);

    Optional<T> headOpt() ;

    T head();

    <X> PStream<X> cast(Class<X> itemClass);
    Optional<T> lastOpt();

    Optional<T> beforeLastOpt() ;

    PStream<T> replaceFirst(T original, T newOne);

    PStream<T>  tail();

    Optional<T> max(Comparator<T> comp);
    Optional<T> min(Comparator<T> comp);
    Optional<T> min();
    Optional<T> max();

    boolean isEmpty();
    int size();

    int count(Predicate<T> predicate);

    PStream<T> plusAll(T v1,T... rest);

    T[] toArray();

    <T1> T1[] toArray(T1[] a);

    PList<T> plist();

    PSet<T> pset();
    POrderedSet<T> porderedset();

    PStream<T> distinct();


    LList<T> llist();
    List<T> list();

    List<T> toList();


    Optional<T> join(BinaryOperator<T> joiner);
    <X> PStream<X> flatten();
    PStream<String> mapString();
    PStream<String> mapString(String nullValue);

    String toString(String sep);

    String toString(String left, String sep, String right);


}
