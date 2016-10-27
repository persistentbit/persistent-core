package com.persistentbit.core.collections;


import com.persistentbit.core.tuples.Tuple2;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Copyright(c) Peter Muys.
 * This code is base on the PersistentHashMap created by Rich Hickey.
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
public class PMap<K, V> extends AbstractPStreamDirect<Tuple2<K, V>, PMap<K, V>> implements IPMap<K, V>{

  private static final Logger log       = Logger.getLogger(PMap.class.getName());
  private static final Object sNullKey  = new Object();
  private static final PMap   sEmpty    = new PMap(0, null);
  private static final Object sNotFound = new Object();
  final int     size;
  final MapNode root;


  public PMap() {
	this(0, null);
  }

  private PMap(int size, MapNode root) {
	this.size = size;
	this.root = root;
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private static MapNode[] cloneAndSet(MapNode[] array, int i, MapNode a) {
	MapNode[] clone = array.clone();
	clone[i] = a;
	return clone;
  }

  private static Object[] cloneAndSet(Object[] array, int i, Object a) {
	Object[] clone = array.clone();
	clone[i] = a;
	return clone;
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private static Object[] cloneAndSet(Object[] array, int i, Object a, int j,
									  Object b
  ) {
	Object[] clone = array.clone();
	clone[i] = a;
	clone[j] = b;
	return clone;
  }

  private static Object[] removePair(Object[] array, int i) {
	Object[] newArray = new Object[array.length - 2];
	System.arraycopy(array, 0, newArray, 0, 2 * i);
	System.arraycopy(array, 2 * (i + 1), newArray, 2 * i, newArray.length
															- 2 * i);
	return newArray;
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private static MapNode createNode(int shift, Object key1, Object val1,
									int key2hash, Object key2, Object val2
  ) {
	int key1hash = hash(key1);
	if(key1hash == key2hash)
	  return new HashCollisionNode(key1hash, 2, key1, val1, key2, val2);
	Box addedLeaf = new Box(null);

	return BitmapIndexedNode.EMPTY.assoc(shift, key1hash, key1, val1,
										 addedLeaf
	).assoc(shift, key2hash, key2, val2, addedLeaf);
  }

  private static int hash(Object o) {
	return o.hashCode();
  }

  private static int bitPos(int hash, int shift) {
	return 1 << mask(hash, shift);
  }

  static int mask(int hash, int shift) {
	return (hash >>> shift) & 0x01f;
  }

  @Override
  public PStream<Tuple2<K, V>> lazy() {
	return new AbstractPStreamLazy<Tuple2<K, V>>(){
	  @Override
	  public Iterator<Tuple2<K, V>> iterator() {
		return PMap.this.iterator();
	  }

	};

  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Tuple2<K, V>> iterator() {
	final Iterator<?> rootIter = (root == null) ? Collections.emptyIterator() : root.iterator();
	return (Iterator<Tuple2<K, V>>) rootIter;

  }

  @Override
  protected PMap<K, V> toImpl(PStream<Tuple2<K, V>> lazy) {
	PMap<K, V> r = empty();
	return r.plusAll(lazy);
  }

  @SuppressWarnings("unchecked")
  public static <K, V> PMap<K, V> empty() {
	return (PMap<K, V>) sEmpty;
  }

  @Override
  public PMap<K, V> plusAll(Iterable<? extends Tuple2<K, V>> iter) {
	PMap<K, V> r = this;
	for(Tuple2<K, V> t : iter) {
	  r = r.plus(t);
	}
	return r;
  }

  @Override
  public PMap<K, V> plus(Tuple2<K, V> value) {
	return this.put(value._1, value._2);
  }

  @SuppressWarnings("unchecked")
  @Override
  public PMap<K, V> put(K key, V val) {
	if(key == null) { key = (K) sNullKey; }
	Box     addedLeaf = new Box(null);
	MapNode newRoot   = (root == null ? BitmapIndexedNode.EMPTY : root).assoc(0, hash(key), key, val, addedLeaf);
	if(newRoot == root)
	  return this;
	return new PMap<>(addedLeaf.val == null ? size
						: size + 1, newRoot);
  }

  @Override
  public boolean contains(Object value) {
	if(value == null) {
	  return false;
	}
	if(value instanceof Tuple2 == false) {
	  log.warning("calling PMap.contains without a Tuple2 value");
	  return false;
	}
	Tuple2 tup = (Tuple2) value;
	if(containsKey(tup._1) == false) {
	  return false;
	}
	return Objects.equals(get(tup._1), tup._2);
  }

  @Override
  public boolean containsKey(Object key) {
	if(key == null) { key = sNullKey; }
	return (root != null) && root.find(0, hash(key), key, sNotFound) != sNotFound;
  }

  @Override
  public V get(Object key) {
	return getOrDefault(key, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public V getOrDefault(Object key, V notFound) {
	if(key == null) { key = sNullKey; }
	return (V) (root != null ? root.find(0, hash(key), key, notFound) : notFound);
  }

  @Override
  public <M> PMap<K, M> mapValues(Function<? super V, ? extends M> mapper) {

	PMap<K, M> r = PMap.empty();
	return with(r, (m, e) -> m = m.put(e._1, mapper.apply(e._2)));
  }

  @Override
  public Optional<V> getOpt(Object key) {
	return Optional.ofNullable(getOrDefault(key, null));
  }

  @Override
  public PMap<K, V> removeKey(Object key) {
	if(key == null) { key = sNullKey; }

	if(root == null)
	  return this;
	MapNode newRoot = root.without(0, hash(key), key);
	if(newRoot == root)
	  return this;
	return new PMap<>(size - 1, newRoot);
  }

  @Override
  public PStream<K> keys() {
	return map(e -> e._1);
  }

  @Override
  public PStream<V> values() {
	return map(e -> e._2);
  }

  @Override
  public Map<K, V> map() {
	return new PMapMap<>(this);
  }

  @Override
  public <K2, V2> PMap<K2, V2> mapKeyValues(Function<? super Tuple2<K, V>, ? extends Tuple2<K2, V2>> items) {
	PMap<K2, V2> res = PMap.empty();
	return with(res, (r, t) -> r.plus(items.apply(t)));

  }

  @Override
  public boolean isEmpty() {
	return size == 0;
  }

  @Override
  public PMap<K, V> distinct() {
	return this;
  }

  @Override
  public boolean equals(Object o) {
	if(o == this) {
	  return true;
	}
	if(o instanceof IPMap == false) {
	  return false;
	}
	IPMap other = (IPMap) o;
	if(other.size() != size()) {
	  return false;
	}
	for(Tuple2 entry : this) {
	  Object v1 = entry._2;
	  Object v2 = other.get(entry._1);
	  if(v1 == null) {
		return v2 == null;
	  }
	  if(v1.equals(v2) == false) {
		return false;
	  }
	}
	return true;
  }

  @Override
  public int size() {
	return size;
  }

  interface MapNode extends Serializable{

	MapNode assoc(int shift, int hash, Object key, Object val, Box addedLeaf);

	MapNode without(int shift, int hash, Object key);

	PMapEntry find(int shift, int hash, Object key);

	Object find(int shift, int hash, Object key, Object notFound);

	Iterator iterator();
  }

  private static final class Box{

	public Object val;

	private Box(Object val) {
	  this.val = val;
	}
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final class ArrayNode implements MapNode{

	final int       count;
	final MapNode[] array;
	//final AtomicReference<Thread> edit;

	private ArrayNode(int count, MapNode[] array) {
	  this.array = array;
	  //this.edit = edit;
	  this.count = count;
	}

	@Override
	public MapNode assoc(int shift, int hash, Object key, Object val,
						 Box addedLeaf
	) {
	  int     idx  = mask(hash, shift);
	  MapNode node = array[idx];
	  if(node == null)
		return new ArrayNode(count + 1, cloneAndSet(array, idx,
													BitmapIndexedNode.EMPTY.assoc(shift + 5, hash, key,
																				  val, addedLeaf
													)
		));
	  MapNode n = node.assoc(shift + 5, hash, key, val, addedLeaf);
	  if(n == node)
		return this;
	  return new ArrayNode(count, cloneAndSet(array, idx, n));
	}

	@Override
	public MapNode without(int shift, int hash, Object key) {
	  int     idx  = mask(hash, shift);
	  MapNode node = array[idx];
	  if(node == null)
		return this;
	  MapNode n = node.without(shift + 5, hash, key);
	  if(n == node)
		return this;
	  if(n == null) {
		if(count <= 8) // shrink
		  return pack(idx);
		return new ArrayNode(count - 1,
							 cloneAndSet(array, idx, null)
		);
	  }
	  else
		return new ArrayNode(count, cloneAndSet(array, idx, n));
	}

	private MapNode pack(int idx) {
	  Object[] newArray = new Object[2 * (count - 1)];
	  int      j        = 1;
	  int      bitmap   = 0;
	  for(int i = 0; i < idx; i++)
		if(array[i] != null) {
		  newArray[j] = array[i];
		  bitmap |= 1 << i;
		  j += 2;
		}
	  for(int i = idx + 1; i < array.length; i++)
		if(array[i] != null) {
		  newArray[j] = array[i];
		  bitmap |= 1 << i;
		  j += 2;
		}
	  return new BitmapIndexedNode(bitmap, newArray);
	}

	@Override
	public PMapEntry find(int shift, int hash, Object key) {
	  int     idx  = mask(hash, shift);
	  MapNode node = array[idx];
	  if(node == null)
		return null;
	  return node.find(shift + 5, hash, key);
	}

	@Override
	public Object find(int shift, int hash, Object key, Object notFound) {
	  int     idx  = mask(hash, shift);
	  MapNode node = array[idx];
	  if(node == null)
		return notFound;
	  return node.find(shift + 5, hash, key, notFound);
	}

	@Override
	public Iterator<Object> iterator() {
	  return new Iter(array);
	}

	static final class Iter implements Iterator{

	  private final MapNode[] array;
	  private       int       i;
	  private       Iterator  nestedIter;

	  private Iter(MapNode[] array) {
		this.array = array;
	  }

	  @Override
	  public Object next() {
		if(hasNext())
		  return nestedIter.next();
		else
		  throw new IllegalStateException();
	  }

	  @Override
	  public boolean hasNext() {
		while(true) {
		  if(nestedIter != null)
			if(nestedIter.hasNext())
			  return true;
			else
			  nestedIter = null;

		  if(i < array.length) {
			MapNode node = array[i++];
			if(node != null)
			  nestedIter = node.iterator();
		  }
		  else
			return false;
		}
	  }

	  @Override
	  public void remove() {
		throw new UnsupportedOperationException();
	  }
	}
  }

  private static final class BitmapIndexedNode implements MapNode{

	static final BitmapIndexedNode EMPTY = new BitmapIndexedNode(0, new Object[0]);

	final int      bitmap;
	final Object[] array;


	private BitmapIndexedNode(int bitmap,
							  Object[] array
	) {
	  this.bitmap = bitmap;
	  this.array = array;

	}

	@Override
	public MapNode assoc(int shift, int hash, Object key, Object val,
						 Box addedLeaf
	) {
	  int bit = bitPos(hash, shift);
	  int idx = index(bit);
	  if((bitmap & bit) != 0) {
		Object keyOrNull = array[2 * idx];
		Object valOrNode = array[2 * idx + 1];
		if(keyOrNull == null) {
		  MapNode n = ((MapNode) valOrNode).assoc(shift + 5, hash, key,
												  val, addedLeaf
		  );
		  if(n == valOrNode)
			return this;
		  return new BitmapIndexedNode(bitmap, cloneAndSet(
			array, 2 * idx + 1, n));
		}
		if(key.equals(keyOrNull)) {
		  if(val == valOrNode)
			return this;
		  return new BitmapIndexedNode(bitmap, cloneAndSet(
			array, 2 * idx + 1, val));
		}
		addedLeaf.val = addedLeaf;
		return new BitmapIndexedNode(bitmap, cloneAndSet(
		  array,
		  2 * idx,
		  null,
		  2 * idx + 1,
		  createNode(shift + 5, keyOrNull, valOrNode, hash, key,
					 val
		  )
		));
	  }
	  else {
		int n = Integer.bitCount(bitmap);
		if(n >= 16) {
		  MapNode[] nodes = new MapNode[32];
		  int       jdx   = mask(hash, shift);
		  nodes[jdx] = EMPTY.assoc(shift + 5, hash, key, val,
								   addedLeaf
		  );
		  int j = 0;
		  for(int i = 0; i < 32; i++)
			if(((bitmap >>> i) & 1) != 0) {
			  if(array[j] == null)
				nodes[i] = (MapNode) array[j + 1];
			  else
				nodes[i] = EMPTY.assoc(shift + 5,
									   hash(array[j]), array[j], array[j + 1],
									   addedLeaf
				);
			  j += 2;
			}
		  return new ArrayNode(n + 1, nodes);
		}
		else {
		  Object[] newArray = new Object[2 * (n + 1)];
		  System.arraycopy(array, 0, newArray, 0, 2 * idx);
		  newArray[2 * idx] = key;
		  addedLeaf.val = addedLeaf;
		  newArray[2 * idx + 1] = val;
		  System.arraycopy(array, 2 * idx, newArray, 2 * (idx + 1),
						   2 * (n - idx)
		  );
		  return new BitmapIndexedNode(bitmap | bit, newArray);
		}
	  }
	}

	@Override
	public MapNode without(int shift, int hash, Object key) {
	  int bit = bitPos(hash, shift);
	  if((bitmap & bit) == 0)
		return this;
	  int    idx       = index(bit);
	  Object keyOrNull = array[2 * idx];
	  Object valOrNode = array[2 * idx + 1];
	  if(keyOrNull == null) {
		MapNode n = ((MapNode) valOrNode).without(shift + 5, hash, key);
		if(n == valOrNode)
		  return this;
		if(n != null)
		  return new BitmapIndexedNode(bitmap, cloneAndSet(
			array, 2 * idx + 1, n));
		if(bitmap == bit)
		  return null;
		return new BitmapIndexedNode(bitmap ^ bit, removePair(
		  array, idx));
	  }
	  if(key.equals(keyOrNull))
		return new BitmapIndexedNode(bitmap ^ bit, removePair(
		  array, idx));
	  return this;
	}

	final int index(int bit) {
	  return Integer.bitCount(bitmap & (bit - 1));
	}

	@Override
	@SuppressWarnings("unchecked")
	public PMapEntry find(int shift, int hash, Object key) {
	  int bit = bitPos(hash, shift);
	  if((bitmap & bit) == 0)
		return null;
	  int    idx       = index(bit);
	  Object keyOrNull = array[2 * idx];
	  Object valOrNode = array[2 * idx + 1];
	  if(keyOrNull == null)
		return ((MapNode) valOrNode).find(shift + 5, hash, key);
	  if(key.equals(keyOrNull))
		return new PMapEntry(keyOrNull, valOrNode);
	  return null;
	}

	@Override
	public Object find(int shift, int hash, Object key, Object notFound) {
	  int bit = bitPos(hash, shift);
	  if((bitmap & bit) == 0)
		return notFound;
	  int    idx       = index(bit);
	  Object keyOrNull = array[2 * idx];
	  Object valOrNode = array[2 * idx + 1];
	  if(keyOrNull == null)
		return ((MapNode) valOrNode).find(shift + 5, hash, key, notFound);
	  if(key.equals(keyOrNull))
		return valOrNode;
	  return notFound;
	}

        /*public ISeq<Object> nodeSeq() {
			return NodeSeq.create(array);
        }*/

	@Override
	public Iterator<Object> iterator() {
	  return new NodeIter(array);
	}


  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final class HashCollisionNode implements MapNode{

	final int      hash;
	final int      count;
	final Object[] array;

	private HashCollisionNode(int hash, int count, Object... array) {
	  this.hash = hash;
	  this.count = count;
	  this.array = array;
	}

	@Override
	public MapNode assoc(int shift, int hash, Object key, Object val,
						 Box addedLeaf
	) {
	  if(hash == this.hash) {
		int idx = findIndex(key);
		if(idx != -1) {
		  if(array[idx + 1] == val)
			return this;
		  return new HashCollisionNode(hash, count,
									   cloneAndSet(array, idx + 1, val)
		  );
		}
		Object[] newArray = new Object[2 * (count + 1)];
		System.arraycopy(array, 0, newArray, 0, 2 * count);
		newArray[2 * count] = key;
		newArray[2 * count + 1] = val;
		addedLeaf.val = addedLeaf;
		return new HashCollisionNode(hash, count + 1, newArray);
	  }
	  // nest it in a bitmap node
	  return new BitmapIndexedNode(bitPos(this.hash, shift),
								   new Object[]{null, this}
	  ).assoc(shift, hash, key, val,
			  addedLeaf
	  );
	}

	public int findIndex(Object key) {
	  for(int i = 0; i < 2 * count; i += 2) {
		if(key.equals(array[i]))
		  return i;
	  }
	  return -1;
	}

	@Override
	public MapNode without(int shift, int hash, Object key) {
	  int idx = findIndex(key);
	  if(idx == -1)
		return this;
	  if(count == 1)
		return null;
	  return new HashCollisionNode(hash, count - 1, removePair(
		array, idx / 2));
	}

	@Override
	public PMapEntry find(int shift, int hash, Object key) {
	  int idx = findIndex(key);
	  if(idx < 0)
		return null;
	  if(key.equals(array[idx]))
		return new PMapEntry(array[idx], array[idx + 1]);
	  return null;
	}

	@Override
	public Object find(int shift, int hash, Object key, Object notFound) {
	  int idx = findIndex(key);
	  if(idx < 0)
		return notFound;
	  if(key.equals(array[idx]))
		return array[idx + 1];
	  return notFound;
	}

	@Override
	public Iterator iterator() {
	  return new NodeIter(array);
	}
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final class NodeIter implements Iterator<Object>{

	final   Object[] array;
	private int      i;
	private Object   nextEntry;
	private Iterator nextIter;

	private NodeIter(Object[] array) {
	  this.array = array;
	}

	@Override
	public boolean hasNext() {
	  if(nextEntry != null || nextIter != null)
		return true;
	  return advance();
	}

	private boolean advance() {
	  while(i < array.length) {
		Object key       = array[i];
		Object nodeOrVal = array[i + 1];
		i += 2;
		if(key != null) {
		  nextEntry = new PMapEntry(key == sNullKey ? null : key, nodeOrVal);
		  return true;
		}
		else if(nodeOrVal != null) {
		  Iterator iter = ((MapNode) nodeOrVal).iterator();
		  if(iter != null && iter.hasNext()) {
			nextIter = iter;
			return true;
		  }
		}
	  }
	  return false;
	}

	@Override
	public Object next() {
	  Object ret = nextEntry;
	  if(ret != null) {
		nextEntry = null;
		return ret;
	  }
	  else if(nextIter != null) {
		ret = nextIter.next();
		if(!nextIter.hasNext())
		  nextIter = null;
		return ret;
	  }
	  else if(advance())
		return next();
	  throw new IllegalStateException();
	}

	@Override
	public void remove() {
	  throw new UnsupportedOperationException();
	}
  }


}
