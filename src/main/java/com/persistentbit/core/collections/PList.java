package com.persistentbit.core.collections;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

/**
 * Copyright(c) Peter Muys.
 * This code is base on the Persistent List created by Rich Hickey.
 * see copyright notice below.
 * <p>
 * Copyright (c) Rich Hickey. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
public class PList<T> extends AbstractIPList<T, PList<T>> implements Serializable{


	private static final Object[] emptyArray = new Object[0];
	private static final Node     emtpyNode  = new Node();
	private static final PList    emptyPList = new PList();
	//Next ones should be final but can't because of Serializable
	private int      cnt;
	private int      shift;
	private Node     root;
	private Object[] tail;
	private int      tailOffset;

	public PList() {
		this(0, 5, emtpyNode, emptyArray);
	}

	private PList(int cnt, int shift, Node root, Object[] tail) {
		this.cnt = cnt;
		this.shift = shift;
		this.root = root;
		this.tail = tail;
		if(cnt < 32) {
			tailOffset = 0;
		}
		else {
			tailOffset = ((cnt - 1) >>> 5) << 5;
		}
	}

	@SafeVarargs
	public static <T> PList<T> val(T... elements) {
		PList<T> res = PList.empty();
		for(T v : elements) {
			res = res.plus(v);
		}
		return res;
	}

	public static PList<Integer> forInt() {
		return empty();
	}

	@SuppressWarnings("unchecked")
	public static <T> PList<T> empty() {
		return (PList<T>) emptyPList;
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

	public static <V> PList<V> from(Iterable<V> iter) {
		PList<V> res = PList.empty();
		return res.plusAll(iter);
	}


	@Override
	protected PList<T> toImpl(PStream<T> lazy) {
		return lazy.plist();
	}

	@Override
	public PStream<T> lazy() {
		return new LazyPList(0, size());

	}

	private class LazyPList extends AbstractPStreamLazy<T>{

		private final int start;
		private final int end;

		public LazyPList(int start, int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public Iterator<T> iterator() {
			return PList.this.rangedIterator(start, end);
		}

		@Override
		public PList<T> plist() {
			return PList.this;
		}

		@Override
		public PStream<T> tail() {
			if(start == end - 1) {
				throw new IllegalStateException("Tail on empty PStream");
			}
			return new LazyPList(start + 1, end);
		}
	}


	@Override
	public Iterator<T> iterator() {
		return rangedIterator(0, size());
	}

	public Iterator<T> rangedIterator(final int start, final int end) {
		return new Iterator<T>(){
			int i = start;
			int base = i - (i % 32);
			Object[] array = (start < size()) ? arrayFor(i) : null;

			@Override
			public boolean hasNext() {
				return i < end;
			}

			@Override
			@SuppressWarnings("unchecked")
			public T next() {
				if(i - base == 32) {
					array = arrayFor(i);
					base += 32;
				}
				return (T) array[i++ & 0x01f];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}


	@Override
	public int size() {
		return cnt;
	}

	private Object[] arrayFor(int i) {
		if(i < 0 || i > cnt) {
			throw new IndexOutOfBoundsException(" index " + i);
		}
		if(i >= tailOffset) {
			return tail;
		}
		Node node = root;
		for(int level = shift; level > 0; level -= 5)
			node = (Node) node.array[(i >>> level) & 0x01f];
		return node.array;
	}

	public PList<T> subList(int fromIndex, int toIndex) {
		return new PList<T>().plusAll(rangedIterator(fromIndex, toIndex));
	}

	public PList<T> insert(int index, T value) {
		return subList(0, index).plus(value).plusAll(rangedIterator(index, cnt));
	}

	@Override
	public boolean isEmpty() {
		return cnt == 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public PList<T> put(int index, T value) {
		if(index < 0 || index > cnt) {
			throw new IndexOutOfBoundsException(" index " + index);
		}
		if(index == cnt) {
			return plus(value);
		}
		if(index >= tailOffset) {
			Object[] newTail = new Object[tail.length];
			System.arraycopy(tail, 0, newTail, 0, tail.length);
			newTail[index & 0x01f] = value;

			return new PList(cnt, shift, root, newTail);
		}

		return new PList(cnt, shift, doAssoc(shift, root, index, value), tail);
	}

	private Node doAssoc(int level, Node node, int i, Object val) {
		Node ret = new Node(node.array.clone());
		if(level == 0) {
			ret.array[i & 0x01f] = val;
		}
		else {
			int subIndex = (i >>> level) & 0x01f;
			ret.array[subIndex] = doAssoc(level - 5, (Node) node.array[subIndex],
										  i, val
			);
		}
		return ret;
	}

	@Override
	public PList<T> plus(T value) {
		if(cnt - tailOffset < 32) {
			Object[] newTail = new Object[tail.length + 1];
			System.arraycopy(tail, 0, newTail, 0, tail.length);
			newTail[tail.length] = value;
			return new PList<>(cnt + 1, shift, root, newTail);
		}
		// full tail, push into tree
		Node newRoot;
		Node tailNode = new Node(tail);
		int  newShift = shift;
		// overflow root?
		if((cnt >>> 5) > (1 << shift)) {
			newRoot = new Node();
			newRoot.array[0] = root;
			newRoot.array[1] = newPath(shift, tailNode);
			newShift += 5;
		}
		else
			newRoot = pushTail(shift, root, tailNode);
		return new PList<>(cnt + 1, newShift, newRoot, new Object[]{value});
	}

	@Override
	public PList<T> plusAll(Iterable<? extends T> iter) {
		return plusAll(iter.iterator());
	}

	public PList<T> plusAll(Iterator<? extends T> iter) {
		PList<T> res = this;
		while(iter.hasNext()) {
			res = res.plus(iter.next());
		}
		return res;
	}

	private Node pushTail(int level, Node parent, Node tailNode) {
		// if parent is leaf, insert node,
		// else does it map to an existing child? -> nodeToInsert = pushNode one
		// more level
		// else alloc new path
		// return nodeToInsert placed in copy of parent
		int  subIndex = ((cnt - 1) >>> level) & 0x01f;
		Node ret      = new Node(parent.array.clone());
		Node nodeToInsert;
		if(level == 5) {
			nodeToInsert = tailNode;
		}
		else {
			Node child = (Node) parent.array[subIndex];
			nodeToInsert = (child != null) ? pushTail(level - 5, child,
													  tailNode
			) : newPath(level - 5, tailNode);
		}
		ret.array[subIndex] = nodeToInsert;
		return ret;
	}

	private Node newPath(int level, Node node) {
		if(level == 0)
			return node;
		Node ret = new Node();
		ret.array[0] = newPath(level - 5, node);
		return ret;
	}

	@Override
	public boolean contains(Object value) {
		for(T v : this) {
			if((v == null) == (value == null)) {
				if(v == null || v.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Optional<T> lastOpt() {
		if(size() == 0) {
			return Optional.empty();
		}
		return Optional.ofNullable(get(size() - 1));
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(int index) {
		Object[] node = arrayFor(index);
		return (T) node[index & 0x01f];
	}

	@Override
	public Optional<T> beforeLastOpt() {
		if(size() < 2) {
			return Optional.empty();
		}
		return Optional.ofNullable(get(size() - 2));
	}

	public int indexOf(Object o) {
		for(int t = 0; t < cnt; t++) {
			T v = get(t);
			if((v == null) == (o == null)) {
				if(v == null || v.equals(o)) {
					return t;
				}
			}
		}
		return -1;
	}


	public int lastIndexOf(Object o) {
		for(int t = cnt - 1; t >= 0; t--) {
			T v = get(t);
			if((v == null) == (o == null)) {
				if(v == null || v.equals(o)) {
					return t;
				}
			}
		}
		return -1;
	}

	@Override
	public List<T> list() {
		return new PListList<>(this);
	}

	@Override
	public PList<T> dropLast() {
		if(cnt == 0)
			throw new IllegalStateException("Can't pop empty PList");
		if(cnt == 1)
			return PList.empty();
		if(cnt - tailOffset > 1) {
			Object[] newTail = new Object[tail.length - 1];
			System.arraycopy(tail, 0, newTail, 0, newTail.length);
			return new PList<>(cnt - 1, shift, root, newTail);
		}
		Object[] newTail = arrayFor(cnt - 2);

		Node newRoot  = popTail(shift, root);
		int  newShift = shift;
		if(newRoot == null) {
			newRoot = emtpyNode;
		}
		if(shift > 5 && newRoot.array[1] == null) {
			newRoot = (Node) newRoot.array[0];
			newShift -= 5;
		}
		return new PList<>(cnt - 1, newShift, newRoot, newTail);
	}

	private Node popTail(int level, Node node) {
		int subIndex = ((cnt - 2) >>> level) & 0x01f;
		if(level > 5) {
			Node newChild = popTail(level - 5, (Node) node.array[subIndex]);
			if(newChild == null && subIndex == 0)
				return null;
			else {
				Node ret = new Node(node.array.clone());
				ret.array[subIndex] = newChild;
				return ret;
			}
		}
		else if(subIndex == 0)
			return null;
		else {
			Node ret = new Node(node.array.clone());
			ret.array[subIndex] = null;
			return ret;
		}
	}

	@Override
	public String toString() {
		String r = "";
		int    c = Math.min(cnt, 100);

		for(int t = 0; t < c; t++) {
			if(t != 0) {
				r += ", ";
			}
			r += String.valueOf(get(t));
		}
		if(c < cnt) {
			r += "...";
		}
		return "[" + r + "]";
	}

	@Override
	public boolean equals(Object o) {
		if(o == this)
			return true;

		Iterator<?> i2;
		if(o instanceof IPList) {
			IPList p = (IPList) o;
			i2 = p.iterator();
		}
		else {
			return false;
		}
		ListIterator<T> i1 = listIterator(0);

		while(i1.hasNext() && i2.hasNext()) {
			T      o1 = i1.next();
			Object o2 = i2.next();
			if(!(o1 == null ? o2 == null : o1.equals(o2)))
				return false;
		}
		return !(i1.hasNext() || i2.hasNext());
	}

	public ListIterator<T> listIterator(int index) {
		return new PListIterator(index);
	}

	@Override
	public PList<T> plist() {
		return this;
	}

	@Override
	public <R> PList<R> map(Function<? super T, ? extends R> mapper) {
		PList<R> res = PList.empty();
		for(T v : this) {
			res = res.plus(mapper.apply(v));
		}
		return res;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(size());
		for(T v : this) {
			out.writeObject(v);
		}
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		int   size = in.readInt();
		PList v    = empty();
		for(int t = 0; t < size; t++) {
			v = v.plus(in.readObject());
		}
		this.cnt = v.cnt;
		this.shift = v.shift;
		this.root = v.root;
		this.tail = v.tail;
		this.tailOffset = v.tailOffset;
	}

	private static final class Node implements Serializable{

		private final Object[] array;

		private Node(Object[] array) {
			this.array = array;
		}

		private Node() {
			this.array = new Object[32];
		}
	}

	private class PListIterator implements ListIterator<T>{

		private int position;

		protected PListIterator(int pos) {
			position = pos;
		}

		@Override
		public final T next() {
			if(!hasNext()) {
				throw new NoSuchElementException();
			}
			return get(position++);
		}

		@Override
		public final boolean hasNext() {
			return position < cnt;
		}

		@Override
		public final int nextIndex() {
			return position;
		}

		@Override
		public final T previous() {
			if(!hasPrevious()) {
				throw new NoSuchElementException();
			}
			return get(--position);
		}

		@Override
		public final boolean hasPrevious() {
			return position > 0;
		}

		@Override
		public final int previousIndex() {
			return position - 1;
		}

		@Override
		public void remove() {
			changeError();

		}

		private void changeError() {
			throw new UnsupportedOperationException("ListIterator over Immutable PList)");
		}

		@Override
		public void set(T t) {
			changeError();

		}

		@Override
		public void add(T t) {
			changeError();
		}
	}
}
