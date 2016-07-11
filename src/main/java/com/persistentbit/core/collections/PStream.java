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

    static <T> PStream<T> from(Optional<T> opt){
        if(opt.isPresent()){
            return val(opt.get());
        }
        return val();
    }

    static <T> PStream<T> from(Iterable<T> iter){
        if(iter instanceof PStream){
            return ((PStream<T>)iter);
        }
        if(iter instanceof PStreamable){
            return ((PStreamable)iter).asPStream();
        }
        return new PStreamLazy<T>() {
            @Override
            public Iterator<T> iterator() {
                return iter.iterator();
            }

        };
    }
    static <T> PStream<T> from(T[] values){
        if(values == null){
            return PList.empty();
        }
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

    static <T> PStream<T> val(T...values){
        return from((T[])values);
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
                    Iterator<T> master = PStream.this.iterator();
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
                    Iterator<T> master = PStream.this.iterator();
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


    default <R> PStream<R> map(Function<T, R> mapper){

        return new PStreamLazy<R>(){
            @Override
            public boolean isInfinit() {
                return PStream.this.isInfinit();
            }

            @Override
            public Iterator<R> iterator() {
                Iterator<T> master = PStream.this.iterator();
                return new Iterator<R>() {
                    @Override
                    public boolean hasNext() {
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





    default PStream<T> filter(Predicate<T> p){
        return new PStreamLazy<T>(){
            @Override
            public boolean isInfinit() {
                return PStream.this.isInfinit();
            }
            @Override
            public Iterator<T> iterator() {
                return new FilteredIterator<T>(p,PStream.this.iterator());
            }


        };

    }

    default Optional<T> find(Predicate<T> p){
        for(T v : this){
            if(p.test(v)){
                return Optional.ofNullable(v);
            }
        }
        return Optional.empty();


    }

    default <Z> PStream<Tuple2<Z,T>> zip(PStream<Z> zipStream){
        return new PStreamLazy<Tuple2<Z, T>>() {
            @Override
            public boolean isInfinit() {
                return PStream.this.isInfinit() && zipStream.isInfinit();
            }

            @Override
            public Iterator<Tuple2<Z, T>> iterator() {
                Iterator<Z> iz = zipStream.iterator();
                Iterator<T> it = PStream.this.iterator();
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
                    Iterator<T> thisIter = PStream.this.iterator();
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




    default PStream<T> plusAll(Iterable<T> iter){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        return new PStreamAnd<>(this,PStream.from(iter));
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


    default <K> PMap<K,PList<T>> groupBy(Function<T, K> keyGen){
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


    default PStream<T> plus(T value){
        if(isInfinit()){ throw new InfinitePStreamException(); }

        return new PStreamLazy<T>() {

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    Iterator<T> master = PStream.this.iterator();
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

    default PStream<T>  tail() {
        if(isEmpty()){
            throw new IllegalStateException("Tail of empty stream");
        }
        return new PStreamLazy<T>() {
            @Override
            public boolean isInfinit() {
                return PStream.this.isInfinit();
            }

            @Override
            public Iterator<T> iterator() {
                Iterator<T> iter = PStream.this.iterator();
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

    default int count(Predicate<T> predicate){
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
                return new FilteredIterator<T>(distinct,PStream.this.iterator());
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
        return Optional.of(res);
    }

    default <X> PStream<X> flatten() {
        return new PStreamLazy<X>() {
            @Override
            public Iterator<X> iterator() {
                return new FlattenIterator<X>(PStream.this.iterator());
            }
        };
    }


    default String toString(String sep){
        return toString("",sep,"");
    }


    default String toString(String left, String sep, String right){
        return left + map(i-> "" + i).join((a,b)-> a + sep + b).orElse("") + right;
    }


}
