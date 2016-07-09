package com.persistentbit.core.collections;



import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Peter Muys
 * @since 9/06/2016
 */
public abstract class LList<E> extends AbstractPSeq<E,LList<E>> implements Serializable{

    static private final  LList  empty = new LList() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public Optional headOpt() {
            return Optional.empty();
        }

        @Override
        public Optional<LList> tailOption() {
            return Optional.empty();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Iterator iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public String toString() {
            return "Nil";
        }


        public Object get(int i) {
            throw new IndexOutOfBoundsException("empty LList");
        }


        public PSeq put(int index, Object value) {
            throw new IndexOutOfBoundsException("empty LList");
        }

        @Override
        public LList plusAll(Iterable right) {

            return PStream.from(right).llist();

        }

        @Override
        public LList cons(Object item) {
            return prepend(item);
        }

        @Override
        public LList constAll(Iterable right) {
            return PStream.from(right).llist();
        }
    };

    static public <T> LList<T> empty() {
        return (LList<T>) empty;
    }

    @Override
    LList<E> toImpl(PStream<E> lazy) {
        return lazy.llist();
    }



    private class LListImpl<E> extends LList<E>{
        private final E head;
        private final LList<E>  tail;

        public LListImpl(E head, LList<E> tail) {
            this.head = head;
            this.tail = Objects.requireNonNull(tail,"tail");

        }



        @Override
        public Optional<E> headOpt() {
            return Optional.ofNullable(head);
        }

        @Override
        public Optional<LList<E>> tailOption() {
            return Optional.of(tail);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private LList<E> next;
                @Override
                public boolean hasNext() {
                    if(next == null) {
                        return true;
                    }
                    return next.isEmpty() == false;
                }

                @Override
                public E next() {
                    if(next == null){
                        next = tail;
                        return head;
                    }
                    E item = next.head();
                    next = next.tail();
                    return item;
                }
            };
        }


        public E get(int i) {
            int cnt = i;
            Iterator<E> iter = iterator();
            while(iter.hasNext()){
                if(cnt ==i){
                    return iter.next();
                }
                iter.next();
            }
            throw new IndexOutOfBoundsException("" + i);
        }


        public PSeq<E> put(int index, E value) {
            LList<E> r = LList.empty;
            if(index <0){
                throw new IndexOutOfBoundsException("< 0");
            }
            for(E v : this){
                if(index == 0){
                    r = r.prepend(value);
                } else {
                    r = r.prepend(v);
                }
                index--;
            }
            if(index >=0){
                throw new IndexOutOfBoundsException();
            }
            return reversed();
        }

        @Override
        public String toString() {
            return head.toString() + " :: " + tail.toString();
        }
    }

    public LList<E> prepend(E item){
        return new LListImpl<E>(item,this);
    }



    public LList<E> cons(E item){

        LList<E> r = LList.empty.prepend(item);
        for(E v : this.lazy().reversed()){
            r = r.prepend(v);
        }
        return r;
    }



    public LList<E> constAll(Iterable<E> right){
        return plusAll(right).llist();
    }




    @Override
    public LList<E> reversed() {
        LList<E> r = LList.empty;
        for(E e : this){
            r = r.prepend(e);
        }
        return r;
    }



    abstract public Optional<E> headOpt();

    public LList<E>  tail() {
        return tailOption().get();
    }
    public abstract Optional<LList<E>> tailOption() ;

    public abstract boolean isEmpty();



    static public void main(String...args){
        LList<Integer> li = LList.empty;
        dump(li);
        li = li.prepend(1);
        dump(li);
        li = li.prepend(2);
        dump(li);
        li = li.prepend(3);
        dump(li);

        for(int t=0; t< 1000000000; t++){
            if(t % 1000000 == 0){
                System.out.println(t);
            }
            li = li.prepend(t);
        }


        System.out.println("done");
    }



    static void dump(LList<Integer> list){
        int count = 0;
        System.out.println("");
        System.out.println("dump list size = " + list.size());
        for(Integer item : list.reversed()){
            System.out.println("Item " + count + " = " + item);
            count++;
        }
    }
}
