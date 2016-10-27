package com.persistentbit.core.collections;



import com.persistentbit.core.tuples.Tuple2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

/**
 *   Copyright(c) Peter Muys.
 *   This code is base on the Persistent List created by Rich Hickey.
 *   see copyright notice below.
 *
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 */
public class PList<T> extends AbstractIPList<T,PList<T>> implements Serializable{



    private static final Object[] emptyArray = new Object[0];
    private static final Node     emtpyNode  = new Node();
    private static final PList    emptyPList = new PList();


    @SuppressWarnings("unchecked")
    public static <T> PList<T> empty() {
        return (PList<T>) emptyPList;
    }

    private static final class Node implements Serializable {
        private final Object[] array;
        private Node(Object[] array) {
            this.array = array;
        }
        private Node() {
            this.array = new Object[32];
        }
    }

    //Next ones should be final but can't because of Serializable
    private int cnt;
    private int shift;
    private Node root;
    private Object[] tail;
    private int tailOffset;


    @Override
    protected PList<T> toImpl(PStream<T> lazy) {
        return lazy.plist();
    }

    public PList() {
        this(0, 5, emtpyNode, emptyArray);
    }



    private PList(int cnt, int shift, Node root, Object[] tail) {
        this.cnt = cnt;
        this.shift = shift;
        this.root = root;
        this.tail = tail;
        if (cnt < 32) {
            tailOffset = 0;
        } else {
            tailOffset = ((cnt - 1) >>> 5) << 5;
        }
    }

    @SafeVarargs
    public static <T> PList<T> val(T...elements){
        PList<T> res = PList.empty();
        for(T v: elements){
            res = res.plus(v);
        }
        return res;
    }

    @Override
    public PStream<T> lazy() {
        return new PStreamLazy<T>() {
            @Override
            public Iterator<T> iterator() {
                return PList.this.iterator();
            }

            @Override
            public PList<T> plist() {
                return PList.this;
            }

        };

    }

    public static PList<Integer> forInt() {
        return empty();
    }
    public static PList<Long> forLong() {
        return empty();
    }

    public static PList<String> forString() {
        return empty();
    }
    public static PList<Boolean> forBoolean() {
        return empty();
    }
    public static <V> PList<V> from(Iterable<V> iter){
        PList<V> res = PList.empty();
        return res.plusAll(iter);
    }


    public  PList<T> subList(int fromIndex, int toIndex){
        return new PList<T>().plusAll(rangedIterator(fromIndex,toIndex));
    }

    public PList<T> insert(int index,T value){
        return subList(0,index).plus(value).plusAll(rangedIterator(index,cnt));
    }

    public int size() {
        return cnt;
    }

    public boolean isEmpty(){
        return cnt == 0 ;
    }

    @SuppressWarnings("unchecked")
    public T get(int i) {
        Object[] node = arrayFor(i);
        return (T)node[i & 0x01f];
    }
    private Object[] arrayFor(int i) {
        if(i<0 || i > cnt){
            throw new IndexOutOfBoundsException(" index " + i);
        }
        if (i >= tailOffset) {
            return tail;
        }
        Node node = root;
        for (int level = shift; level > 0; level -= 5)
            node = (Node) node.array[(i >>> level) & 0x01f];
        return node.array;
    }
    @SuppressWarnings("unchecked")
    public PList<T> put(int i, T val) {
        if(i<0 || i > cnt){
            throw new IndexOutOfBoundsException(" index " + i);
        }
        if(i == cnt){
            return plus(val);
        }
        if (i >= tailOffset) {
            Object[] newTail = new Object[tail.length];
            System.arraycopy(tail, 0, newTail, 0, tail.length);
            newTail[i & 0x01f] = val;

            return new PList(cnt, shift, root, newTail);
        }

        return new PList(cnt, shift, doAssoc(shift, root, i, val), tail);
    }
    private Node doAssoc(int level, Node node, int i, Object val) {
        Node ret = new Node(node.array.clone());
        if (level == 0) {
            ret.array[i & 0x01f] = val;
        } else {
            int subidx = (i >>> level) & 0x01f;
            ret.array[subidx] = doAssoc(level - 5, (Node) node.array[subidx],
                    i, val);
        }
        return ret;
    }


    @Override
    public PList<T> plus(T val) {
        if (cnt - tailOffset < 32) {
            Object[] newTail = new Object[tail.length + 1];
            System.arraycopy(tail, 0, newTail, 0, tail.length);
            newTail[tail.length] = val;
            return new PList<>(cnt + 1, shift, root, newTail);
        }
        // full tail, push into tree
        Node newroot;
        Node tailnode = new Node(tail);
        int newshift = shift;
        // overflow root?
        if ((cnt >>> 5) > (1 << shift)) {
            newroot = new Node();
            newroot.array[0] = root;
            newroot.array[1] = newPath(shift, tailnode);
            newshift += 5;
        } else
            newroot = pushTail(shift, root, tailnode);
        return new PList<>(cnt + 1, newshift, newroot, new Object[]{val});
    }



    @Override
    public PList<T> plusAll(Iterable<? extends T> seq){
        return plusAll(seq.iterator());
    }

    public PList<T> plusAll(Iterator<? extends T> iter){
        PList<T> res = this;
        while(iter.hasNext()){
            res = res.plus(iter.next());
        }
        return res;
    }



    private Node pushTail(int level, Node parent, Node tailnode) {
        // if parent is leaf, insert node,
        // else does it map to an existing child? -> nodeToInsert = pushNode one
        // more level
        // else alloc new path
        // return nodeToInsert placed in copy of parent
        int subidx = ((cnt - 1) >>> level) & 0x01f;
        Node ret = new Node(parent.array.clone());
        Node nodeToInsert;
        if (level == 5) {
            nodeToInsert = tailnode;
        } else {
            Node child = (Node) parent.array[subidx];
            nodeToInsert = (child != null) ? pushTail(level - 5, child,
                    tailnode) : newPath(level - 5, tailnode);
        }
        ret.array[subidx] = nodeToInsert;
        return ret;
    }

    private Node newPath(int level, Node node) {
        if (level == 0)
            return node;
        Node ret = new Node();
        ret.array[0] = newPath(level - 5, node);
        return ret;
    }
    public Iterator<T> iterator() {
        return rangedIterator(0, size());
    }
    public Iterator<T> rangedIterator(final int start, final int end) {
        return new Iterator<T>() {
            int i = start;
            int base = i - (i % 32);
            Object[] array = (start < size()) ? arrayFor(i) : null;

            public boolean hasNext() {
                return i < end;
            }

            @SuppressWarnings("unchecked")
            public T next() {
                if (i - base == 32) {
                    array = arrayFor(i);
                    base += 32;
                }
                return (T)array[i++ & 0x01f];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean contains(Object o) {
        for(T v : this){
            if((v==null) == (o==null)){
                if(v == null || v.equals(o)){
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public Optional<T> lastOpt() {
        if(size() == 0){
            return Optional.empty();
        }
        return Optional.ofNullable(get(size()-1));
    }

    @Override
    public Optional<T> beforeLastOpt() {
        if(size() <2){
            return Optional.empty();
        }
        return Optional.ofNullable(get(size()-2));
    }

    public int indexOf(Object o) {
        for(int t=0; t<cnt;t++){
            T v = get(t);
            if((v==null) == (o==null)){
                if(v == null || v.equals(o)){
                    return t;
                }
            }
        }
        return -1;
    }


    public int lastIndexOf(Object o) {
        for(int t=cnt-1; t>=0;t--){
            T v = get(t);
            if((v==null) == (o==null)){
                if(v == null || v.equals(o)){
                    return t;
                }
            }
        }
        return -1;
    }

    public ListIterator<T> listIterator(int index) {
        return new PListIterator(index);
    }
    private class PListIterator implements ListIterator<T> {
        private int position;

        protected PListIterator(int pos) {
            position = pos;
        }

        @Override
        public final boolean hasNext() {
            return position < cnt;
        }

        @Override
        public final T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return get(position++);
        }

        @Override
        public final int nextIndex() {
            return position;
        }

        @Override
        public final boolean hasPrevious() {
            return position > 0;
        }

        @Override
        public final T previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            return get(--position);
        }

        @Override
        public final int previousIndex() {
            return position - 1;
        }

        @Override
        public void remove() {
            changeError();

        }

        @Override
        public void set(T t) {
            changeError();

        }

        @Override
        public void add(T t) {
            changeError();
        }
        private void changeError(){
            throw new UnsupportedOperationException("ListIterator over Immutable PList)");
        }
    }

    @Override
    public List<T> list() {
        return new PListList<>(this);
    }


    public PList<T> dropLast() {
        if (cnt == 0)
            throw new IllegalStateException("Can't pop empty PList");
        if (cnt == 1)
            return PList.empty();
        if (cnt - tailOffset > 1) {
            Object[] newTail = new Object[tail.length - 1];
            System.arraycopy(tail, 0, newTail, 0, newTail.length);
            return new PList<>(cnt - 1, shift, root, newTail);
        }
        Object[] newtail = arrayFor(cnt - 2);

        Node newroot = popTail(shift, root);
        int newshift = shift;
        if (newroot == null) {
            newroot = emtpyNode;
        }
        if (shift > 5 && newroot.array[1] == null) {
            newroot = (Node) newroot.array[0];
            newshift -= 5;
        }
        return new PList<>(cnt - 1, newshift, newroot, newtail);
    }

    private Node popTail(int level, Node node) {
        int subidx = ((cnt - 2) >>> level) & 0x01f;
        if (level > 5) {
            Node newchild = popTail(level - 5, (Node) node.array[subidx]);
            if (newchild == null && subidx == 0)
                return null;
            else {
                Node ret = new Node(node.array.clone());
                ret.array[subidx] = newchild;
                return ret;
            }
        } else if (subidx == 0)
            return null;
        else {
            Node ret = new Node(node.array.clone());
            ret.array[subidx] = null;
            return ret;
        }
    }

    @Override
    public String toString() {
        String r = "";
        int c = Math.min(cnt,100);

        for(int t=0; t<c;t++){
            if(t != 0){
                r += ", ";
            }
            r+= "" + get(t);
        }
        if(c < cnt){
            r += "...";
        }
        return "[" + r + "]";
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        Iterator<?> i2;
        if(o instanceof IPList) {
            IPList p = (IPList)o;
            i2 = p.iterator();
        }else {
            return false;
        }
        ListIterator<T> i1 = listIterator(0);

        while (i1.hasNext() && i2.hasNext()) {
            T o1 = i1.next();
            Object o2 = i2.next();
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }
        return !(i1.hasNext() || i2.hasNext());
    }

    @Override
    public PList<T> plist() {
        return this;
    }


    @Override
    public <R> PList<R> map(Function<? super T,? extends R> mapper) {
        PList<R> res = PList.empty();
        for(T v:this){
            res = res.plus(mapper.apply(v));
        }
        return res;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(size());
        for(T v : this){
            out.writeObject(v);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        int size = in.readInt();
        PList v = empty();
        for(int t=0; t< size; t++){
            v = v.plus(in.readObject());
        }
        this.cnt = v.cnt;
        this.shift = v.shift;
        this.root = v.root;
        this.tail = v.tail;
        this.tailOffset = v.tailOffset;
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String...args){
        PList<Integer> l = new PList<>();
        for(int t=0; t<100000;t++){
            l = l.plus(t);
        }
        PList<Integer> l2 = l;
        for(int t=0; t<l.size();t++){
            if(l.get(t) != t){
                throw new RuntimeException();
            }
            l2 = l2.put(t,-t);
        }
        for(int t=0; t<l2.size();t++){
            if(l2.get(t) != -t){
                throw new RuntimeException("t=" + t + ", value=" + l2.get(t) );
            }
        }
        PList<Integer> p = new PList<>();
        p = p.plusAll(1,2,3,4,5,6,7,8,9,10);
        System.out.println(p);
        System.out.println(p.dropLast());
        System.out.println(p.subList(0,1));
        System.out.println(p.map(i-> "("+i+")"));
        System.out.println(p.filter(i-> i % 2==0).map(i-> "("+i+")"));
        p = new PList<Integer>().plusAll(4,1,2,7,0,3,10,-5);
        PStream<Tuple2<Integer,Integer>> p2 = p.sorted().zipWithIndex().plist().reversed();
        System.out.println(p2);
        System.out.println(p2.plist());

        System.out.println("AsArrayList: " + p2.groupBy((t)-> t._1).mapValues(v -> v.headOpt().map(i-> i._2).orElse(null)).toList());
    }
}
