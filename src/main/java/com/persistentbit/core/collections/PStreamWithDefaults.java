package com.persistentbit.core.collections;

import com.persistentbit.core.tuples.Tuple2;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * User: petermuys
 * Date: 20/07/16
 * Time: 17:42
 */
public interface PStreamWithDefaults<T> extends PStream<T>{



    default PStream<T> lazy() {
        return this;
    }




    default boolean isInfinit() {
        return false;
    }




    default PStream<T> clear(){
        return new PStreamLazy<T>(){
            @Override
            public Iterator<T> iterator() {
                return Collections.emptyIterator();
            }

        };
    }



    default PStream<T> limit(int count){
        if(count < 0){
            throw new IndexOutOfBoundsException("count can't be < 0: " + count);
        }
        return new PStreamLazy<T>() {


            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    int cnt = count;
                    Iterator<T> master = PStreamWithDefaults.this.iterator();
                    @Override
                    public boolean hasNext() {
                        return cnt>0 && master.hasNext();
                    }

                    @Override
                    public T next() {
                        if(cnt <= 0){
                            throw new IllegalStateException("Over limit");
                        }
                        cnt--;
                        return master.next();
                    }
                };
            }
        };
    }
    default PStream<T>  dropLast(){
        if(isInfinit()){ throw new InfinitePStreamException();}

        return new PStreamLazy<T>() {

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    Iterator<T> master = PStreamWithDefaults.this.iterator();
                    boolean hasValue = master.hasNext();
                    T value = (hasValue ? master.next() : null);
                    @Override
                    public boolean hasNext() {
                        return master.hasNext();
                    }

                    @Override
                    public T next() {
                        T res = value;
                        hasValue = master.hasNext();
                        value = (hasValue ? master.next() : null);
                        return res;
                    }
                };
            }

        };
    }


    default <R> PStream<R> map(Function<? super T,? extends R> mapper){

        return new PStreamLazy<R>(){
            @Override
            public boolean isInfinit() {
                return PStreamWithDefaults.this.isInfinit();
            }

            @Override
            public Iterator<R> iterator() {

                return new Iterator<R>() {
                    Iterator<T> master = null;
                    @Override
                    public boolean hasNext() {
                        if(master == null){
                            master = PStreamWithDefaults.this.iterator();
                        }
                        return master.hasNext();
                    }

                    @Override
                    public R next() {
                        return mapper.apply(master.next());
                    }
                };
            }

        };
    }





    default PStream<T> filter(Predicate<? super T> p){
        return new PStreamLazy<T>(){
            @Override
            public boolean isInfinit() {
                return PStreamWithDefaults.this.isInfinit();
            }
            @Override
            public Iterator<T> iterator() {
                return new FilteredIterator<T>(p,PStreamWithDefaults.this.iterator());
            }


        };

    }

    @Override
    default PStream<T> filterNulls() {
        return filter(x -> x != null);
    }

    default Optional<T> find(Predicate<? super T> p){
        for(T v : this){
            if(p.test(v)){
                return Optional.ofNullable(v);
            }
        }
        return Optional.empty();


    }

    default PStream<Tuple2<HeadMiddleEnd,T>>    headMiddleEnd() {
        return new PStreamLazy<Tuple2<HeadMiddleEnd, T>>() {
            @Override
            public boolean isInfinit() {
                return PStreamWithDefaults.this.isInfinit();
            }

            @Override
            public Iterator<Tuple2<HeadMiddleEnd, T>> iterator() {
                Iterator<T> it = PStreamWithDefaults.this.iterator();
                return new Iterator<Tuple2<HeadMiddleEnd, T>>() {
                    private HeadMiddleEnd current = null;
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Tuple2<HeadMiddleEnd, T> next() {
                        T result = it.next();
                        if(current == null){
                            current = HeadMiddleEnd.head;
                            if(it.hasNext() == false){
                                current = HeadMiddleEnd.headAndEnd;
                            }
                        } else {
                            if(it.hasNext()){
                                current = HeadMiddleEnd.middle;
                            } else {
                                current = HeadMiddleEnd.end;
                            }
                        }
                        return new Tuple2<HeadMiddleEnd, T>(current,result);
                    }
                };
            }
        };
    }


    default <Z> PStream<Tuple2<Z,T>> zip(PStream<Z> zipStream){
        return new PStreamLazy<Tuple2<Z, T>>() {
            @Override
            public boolean isInfinit() {
                return PStreamWithDefaults.this.isInfinit() && zipStream.isInfinit();
            }

            @Override
            public Iterator<Tuple2<Z, T>> iterator() {
                Iterator<Z> iz = zipStream.iterator();
                Iterator<T> it = PStreamWithDefaults.this.iterator();
                return new Iterator<Tuple2<Z, T>>() {

                    @Override
                    public boolean hasNext() {
                        return iz.hasNext() && it.hasNext();
                    }

                    @Override
                    public Tuple2<Z, T> next() {
                        return new Tuple2<>(iz.next(),it.next());
                    }
                };
            }


        };
    }

    default PStream<Tuple2<Integer,T>> zipWithIndex(){
        return zip(PStream.sequence(0));
    }





    default Stream<T> stream(){
        return list().stream();
    }

    default PStream<T> sorted(Comparator<? super T> comp){
        if(isInfinit()){ throw new InfinitePStreamException(); }
        return new PStreamLazy<T>() {
            private List<T> sorted;
            @Override
            public synchronized Iterator<T> iterator() {
                if(sorted == null){
                    sorted = new ArrayList<T>();
                    Iterator<T> thisIter = PStreamWithDefaults.this.iterator();
                    while(thisIter.hasNext()){
                        sorted.add(thisIter.next());
                    }
                    Collections.sort(sorted,comp);
                }
                return sorted.iterator();
            }


        };
    }
    default PStream<T> sorted() {
        return sorted((a,b)-> ((Comparable)a).compareTo(b));
    }

    default PStream<T> reversed() {
        if(isInfinit()){ throw new InfinitePStreamException(); }

        return new PStreamReversed<T>(this);
    }




    default PStream<T> plusAll(Iterable<? extends T> iter){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        return new PStreamAnd<T>(this,(PStream<T>)PStream.from(iter));
    }

    default PStream<T> flattenPlusAll(Iterable<Iterable<? extends T>> iterIter){

        return PStream.from(iterIter).with((PStream<T>)this, (c,e)-> c.plusAll(e));
    }

    default boolean contains(Object value){
        for(T v : this){
            if(v == null){
                if(value == null) {
                    return true;
                }
            } else if(v.equals(value)){
                return true;
            }

        }
        return false;
    }

    default boolean containsAll(Iterable<?> iter){
        PSet<T> set = this.pset();
        for(Object v : iter){
            if(set.contains(v) == false){
                return false;
            }
        }
        return true;
    }


    default <K> PMap<K,PList<T>> groupBy(Function<? super T, ? extends K> keyGen){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        PMap<K,PList<T>> r = PMap.empty();
        PList<T> emptyList = PList.empty();
        for(T v : this){
            K k = keyGen.apply(v);
            PList<T> l = r.getOrDefault(k,emptyList);
            l = l.plus(v);
            r = r.put(k,l);
        }
        return r;
    }
    default <K> PMap<K,T> groupByOneValue(Function<? super T, ? extends K> keyGen){
        return (PMap<K,T>)groupBy(keyGen).mapValues(l -> l.head());
    }
    default <K,V> PMap<K,PList<V>> groupBy(Function<? super T, ? extends K> keyGen,Function<? super T,? extends V> valGen){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        PMap<K,PList<V>> r = PMap.empty();
        PList<V> emptyList = PList.empty();
        for(T v : this){
            K k = keyGen.apply(v);
            PList<V> l = r.getOrDefault(k,emptyList);
            l = l.plus(valGen.apply(v));
            r = r.put(k,l);
        }
        return r;
    }
    default <K,V> PMap<K,V> groupByOneValue(Function<? super T, ? extends K> keyGen,Function<? super T,? extends V> valGen){
        return (PMap<K,V>)groupBy(keyGen,valGen).mapValues(l -> l.headOpt().orElse(null));
    }

    default <K> POrderedMap<K,PList<T>> groupByOrdered(Function<? super T,? extends K> keyGen){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        POrderedMap<K,PList<T>> r = POrderedMap.empty();
        PList<T> emptyList = PList.empty();
        for(T v : this){
            K k = keyGen.apply(v);
            PList<T> l = r.getOrDefault(k,emptyList);
            l = l.plus(v);
            r = r.put(k,l);
        }
        return r;
    }
    default <K,V> POrderedMap<K,PList<V>> groupByOrdered(Function<? super T,? extends  K> keyGen,Function<? super T,? extends V> valGen){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        POrderedMap<K,PList<V>> r = POrderedMap.empty();
        PList<V> emptyList = PList.empty();
        for(T v : this){
            K k = keyGen.apply(v);
            PList<V> l = r.getOrDefault(k,emptyList);
            l = l.plus(valGen.apply(v));
            r = r.put(k,l);
        }
        return r;
    }

    default PStream<T> plus(T value){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        return new PStreamLazy<T>() {

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    Iterator<T> master = PStreamWithDefaults.this.iterator();
                    boolean valueAdded = false;

                    @Override
                    public boolean hasNext() {
                        return master.hasNext() || (valueAdded==false);
                    }

                    @Override
                    public T next() {
                        if(master.hasNext()){
                            return master.next();
                        }
                        if(valueAdded == false){
                            valueAdded = true;
                            return value;
                        }
                        throw new IllegalStateException();
                    }
                };
            }

        };
    }

    default T fold(T init, BinaryOperator<T> binOp){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        T res = init;
        for(T v : this){
            res = binOp.apply(res,v);
        }
        return res;
    }

    default <X> X with(X init, BiFunction<X, T, X> binOp){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        X res = init;
        for(T v : this){
            res  = binOp.apply(res,v);
        }
        return res;
    }

    default Optional<T> headOpt() {
        Iterator<T> iter = iterator();
        if(iter.hasNext()){
            return Optional.ofNullable(iter.next());
        }
        return Optional.empty();
    }

    default T head() {
        return headOpt().get();
    }


    default <X> PStream<X> cast(Class<X> itemClass){
        return this.map(c -> (X)c);
    }

    default Optional<T> lastOpt(){
        Iterator<T> iter = iterator();
        T last = null;
        while(iter.hasNext()){
            last = iter.next();
        }
        return Optional.ofNullable(last);
    }

    default Optional<T> beforeLastOpt() {
        return dropLast().lastOpt();
    }


    default PStream<T> replaceFirst(T original, T newOne){
        return new PStreamLazy<T>(){
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    boolean found = false;
                    Iterator<T> master;
                    @Override
                    public boolean hasNext() {
                        if(master == null){
                            master = PStreamWithDefaults.this.iterator();
                        }
                        return master.hasNext();
                    }

                    @Override
                    public T next() {
                        T v = master.next();
                        if(original.equals(v)){
                            v = newOne;
                            found = true;
                        }
                        return v;
                    }
                };
            }
        };
    }

    default PStream<T>  tail() {
        if(isEmpty()){
            throw new IllegalStateException("Tail of empty stream");
        }
        return new PStreamLazy<T>() {
            @Override
            public boolean isInfinit() {
                return PStreamWithDefaults.this.isInfinit();
            }

            @Override
            public Iterator<T> iterator() {
                Iterator<T> iter = PStreamWithDefaults.this.iterator();
                if(iter.hasNext()){
                    iter.next();
                }
                return iter;
            }
        };
    }

    default Optional<T> max(Comparator<T> comp){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        return headOpt().map(h -> fold(h,(a, b) -> comp.compare(a,b) >=0 ? a : b));
    }
    default Optional<T> min(Comparator<T> comp){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        return headOpt().map(h -> fold(h,(a, b) -> comp.compare(a,b) <= 0 ? a : b));
    }
    default Optional<T> min() {
        return min((a,b) -> ((Comparable)a).compareTo(b));
    }
    default Optional<T> max() {
        return max((a,b) -> ((Comparable)a).compareTo(b));
    }



    default boolean isEmpty() {
        return iterator().hasNext() == false;
    }

    default int size() {
        if(isInfinit()){ throw new InfinitePStreamException(); }

        int count = 0;
        Iterator<T> iter = iterator();
        while(iter.hasNext()){
            count++;
            iter.next();
        }
        return count;
    }

    default int count(Predicate<? super T> predicate){
        return filter(predicate).size();
    }

    default PStream<T> plusAll(T v1,T... rest){
        if(isInfinit()){ throw new InfinitePStreamException();}

        return plus(v1).plusAll(Arrays.asList(rest));
    }

    default T[] toArray() {
        if(isInfinit()){ throw new InfinitePStreamException();}

        T[] arr =  newArray(size());
        int i = 0;
        for(T v : this){
            arr[i++] = v;
        }
        return arr;
    }

    default <T1> T1[] toArray(T1[] a) {
        if(isInfinit()){ throw new InfinitePStreamException();}

        int size = size();
        if(a.length<size){
            a = Arrays.copyOf(a,size);
        }
        Iterator<T> iter = iterator();
        for(int t=0; t<a.length;t++){
            if(iter.hasNext()){
                a[t] = (T1)iter.next();
            } else {
                a[t] = null;
            }
        }
        return a;
    }


    static <E> E[] newArray(int length, E... array) { return Arrays.copyOf(array, length); }


    default PList<T> plist() {
        if(isInfinit()){ throw new InfinitePStreamException();}

        return new PList<T>().plusAll(this);
    }

    default PSet<T> pset() {
        if(isInfinit()){ throw new InfinitePStreamException();}

        return new PSet<T>().plusAll(this);
    }
    default POrderedSet<T> porderedset() {
        if(isInfinit()){ throw new InfinitePStreamException();}

        return new POrderedSet<T>().plusAll(this);
    }

    default PStream<T> distinct() {
        return new PStreamLazy<T>() {
            @Override
            public Iterator<T> iterator() {
                Set<T> lookup = new HashSet<T>();
                Predicate<T> distinct = v -> {
                    if(lookup.contains(v)){
                        return false;
                    }
                    lookup.add(v);
                    return true;
                };
                return new FilteredIterator<T>(distinct,PStreamWithDefaults.this.iterator());
            }

        };
    }

    default PStream<T> duplicates() {
        if(isInfinit()){ throw new InfinitePStreamException();}
        return new PStreamLazy<T>() {
            @Override
            public Iterator<T> iterator() {
                Set<T> lookup = new HashSet<T>();
                POrderedSet<T> dup = POrderedSet.empty();
                for(T item : PStreamWithDefaults.this){
                    if(lookup.contains(item)){
                        dup = dup.plus(item);
                    }
                    lookup.add(item);
                }
                return dup.iterator();
            }
        };
    }


    default LList<T> llist() {
        if(isInfinit()){ throw new InfinitePStreamException();}


        LList<T> res = LList.empty();
        for (T v : reversed()) {
            res = res.prepend(v);
        }
        return res;
    }
    default List<T> list() {
        if(isInfinit()){ throw new InfinitePStreamException();}


        return plist().list();
    }


    default List<T> toList() {
        if(isInfinit()){ throw new InfinitePStreamException();}

        return new ArrayList<T>(this.list());
    }


    default Optional<T> join(BinaryOperator<T> joiner){
        if(isInfinit()){ throw new InfinitePStreamException();}


        Iterator<T> iter = iterator();
        if(iter.hasNext() == false){
            return Optional.empty();
        }
        T res = iter.next();
        while(iter.hasNext()){
            T sec = iter.next();
            res = joiner.apply(res,sec);
        }
        return Optional.ofNullable(res);
    }

    default <X> PStream<X> flatten() {
        return new PStreamLazy<X>() {
            @Override
            public Iterator<X> iterator() {
                return new FlattenIterator<X>(PStreamWithDefaults.this.iterator());
            }
        };
    }
    default PStream<String> mapString(){
        return mapString("null");
    }
    default PStream<String> mapString(String nullValue) {
        return map(t -> t == null ? nullValue : t.toString());
    }

    default String toString(String sep){
        return toString("",sep,"");
    }


    default String toString(String left, String sep, String right){
        return left + mapString().join((a,b)-> a + sep + b).orElse("") + right;
    }

    @Override
    default PStream<T> peek(Consumer<? super T> consumer) {
        return new PStreamLazy<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    Iterator<T> master;
                    @Override
                    public boolean hasNext() {
                        if(master == null){
                            master = PStreamWithDefaults.this.iterator();
                        }
                        return master.hasNext();
                    }

                    @Override
                    public T next() {
                        if(master == null){
                            master = PStreamWithDefaults.this.iterator();
                        }
                        T element = master.next();
                        consumer.accept(element);
                        return element;
                    }
                };
            }

        };
    }


}
