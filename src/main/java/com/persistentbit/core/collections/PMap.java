package com.persistentbit.core.collections;



import com.persistentbit.core.Tuple2;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
/**
 *   Copyright(c) Peter Muys.
 *   This code is base on the PersistenHashMap created by Rich Hickey.
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
public class PMap<K, V> extends PStreamDirect<Tuple2<K,V>,PMap<K,V>> implements IPMap<K,V>{
    static final private Object sNullKey = new Object();
    static final private PMap sEmpty = new PMap(0, null);
    static public final <K,V> PMap<K,V> empty() {
        return (PMap<K,V>) sEmpty;
    }
    final private static Object sNotFound = new Object();


    final int size;


    final MapNode root;

    public PMap() {
        this(0, null);
    }


    private PMap(int size, MapNode root) {
        this.size = size;
        this.root = root;
    }


    @Override
    public PStream<Tuple2<K,V>> lazy() {
        return new PStreamLazy<Tuple2<K,V>>() {
            @Override
            public Iterator<Tuple2<K,V>> iterator() {
                return PMap.this.iterator();
            }

        };

    }

    @Override
    PMap<K, V> toImpl(PStream<Tuple2<K, V>> lazy) {
        PMap<K,V> r = empty();
        return r.plusAll(lazy);
    }


    @Override
    public PMap<K, V> plus(Tuple2<K, V> value) {
        return this.put(value._1,value._2);
    }



    @Override
    public PMap<K, V> plusAll(Iterable<Tuple2<K, V>> iter) {
        PMap<K,V> r = this;
        for(Tuple2<K,V> t : iter){
            r = r.plus(t);
        }
        return r;
    }

    static private int hash(Object o) {
        return o.hashCode();
    }


    @Override
    public boolean containsKey(Object key) {
        if(key == null) { key = (K)sNullKey; }
            return (root != null) ? root.find(0, hash(key), key, sNotFound) != sNotFound
                : false;
    }

    @Override
    public <M> PMap<K,M> mapValues(Function<V,M> mapper){

        PMap<K,M> r = PMap.empty();
        return with(r,(m,e)-> m = m.put(e._1,mapper.apply(e._2)) );
    }


    @Override
    public PMap<K, V> put(K key, V val) {
        if(key == null) { key = (K)sNullKey; }
        Box addedLeaf = new Box(null);
        MapNode newroot = (root == null ? BitmapIndexedNode.EMPTY : root).assoc(0, hash(key), key, val, addedLeaf);
        if (newroot == root)
            return this;
        return new PMap<>(addedLeaf.val == null ? size
                : size + 1, newroot);
    }





    @Override
    @SuppressWarnings("unchecked")
    public V getOrDefault(Object key, V notFound) {
        if(key == null) { key = sNullKey; }
        return (V) (root != null ? root.find(0, hash(key), key, notFound) : notFound);
    }

    @Override
    public V get(Object key){
        return getOrDefault(key,null);
    }

    @Override
    public Optional<V> getOpt(Object key){
        return Optional.ofNullable(getOrDefault(key,null));
    }


    @Override
    public PMap<K, V> removeKey(Object key) {
        if(key == null) { key = sNullKey; }

        if (root == null)
            return this;
        MapNode newroot = root.without(0, hash(key), key);
        if (newroot == root)
            return this;
        return new PMap<K, V>(size - 1, newroot);
    }

    @Override
    public PStream<K>   keys(){
        return map(e-> e._1);
    }

    @Override
    public PStream<V>   values() {
        return map(e-> e._2);
    }



    @Override
    public Map<K,V> map() {
        return new PMapMap<K,V>(this);
    }





    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        final Iterator<?> rootIter = (root == null) ? Collections.emptyIterator() : root.iterator();
        return (Iterator<Tuple2<K, V>>) rootIter;

    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size ==0;
    }

    static int mask(int hash, int shift) {
        return (hash >>> shift) & 0x01f;
    }


    static private class Box {

        public Object val;

        public Box(Object val) {
            this.val = val;
        }
    }

    interface MapNode extends Serializable {
        MapNode assoc(int shift, int hash, Object key, Object val, Box addedLeaf);

        MapNode without(int shift, int hash, Object key);

        PMapEntry find(int shift, int hash, Object key);

        Object find(int shift, int hash, Object key, Object notFound);

        Iterator iterator();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    final static class ArrayNode implements MapNode {
        int count;
        final MapNode[] array;
        //final AtomicReference<Thread> edit;

        ArrayNode(int count, MapNode[] array) {
            this.array = array;
            //this.edit = edit;
            this.count = count;
        }

        public MapNode assoc(int shift, int hash, Object key, Object val,
                             Box addedLeaf) {
            int idx = mask(hash, shift);
            MapNode node = array[idx];
            if (node == null)
                return new ArrayNode(count + 1, cloneAndSet(array, idx,
                        BitmapIndexedNode.EMPTY.assoc(shift + 5, hash, key,
                                val, addedLeaf)));
            MapNode n = node.assoc(shift + 5, hash, key, val, addedLeaf);
            if (n == node)
                return this;
            return new ArrayNode(count, cloneAndSet(array, idx, n));
        }

        public MapNode without(int shift, int hash, Object key) {
            int idx = mask(hash, shift);
            MapNode node = array[idx];
            if (node == null)
                return this;
            MapNode n = node.without(shift + 5, hash, key);
            if (n == node)
                return this;
            if (n == null) {
                if (count <= 8) // shrink
                    return pack(idx);
                return new ArrayNode(count - 1,
                        cloneAndSet(array, idx, n));
            } else
                return new ArrayNode(count, cloneAndSet(array, idx, n));
        }

        public PMapEntry find(int shift, int hash, Object key) {
            int idx = mask(hash, shift);
            MapNode node = array[idx];
            if (node == null)
                return null;
            return node.find(shift + 5, hash, key);
        }

        public Object find(int shift, int hash, Object key, Object notFound) {
            int idx = mask(hash, shift);
            MapNode node = array[idx];
            if (node == null)
                return notFound;
            return node.find(shift + 5, hash, key, notFound);
        }

        private MapNode pack(int idx) {
            Object[] newArray = new Object[2 * (count - 1)];
            int j = 1;
            int bitmap = 0;
            for (int i = 0; i < idx; i++)
                if (array[i] != null) {
                    newArray[j] = array[i];
                    bitmap |= 1 << i;
                    j += 2;
                }
            for (int i = idx + 1; i < array.length; i++)
                if (array[i] != null) {
                    newArray[j] = array[i];
                    bitmap |= 1 << i;
                    j += 2;
                }
            return new BitmapIndexedNode(bitmap, newArray);
        }


        public Iterator<Object> iterator() {
            return new Iter(array);
        }

        static class Iter implements Iterator {
            private final MapNode[] array;
            private int i = 0;
            private Iterator nestedIter;

            private Iter(MapNode[] array) {
                this.array = array;
            }

            public boolean hasNext() {
                while (true) {
                    if (nestedIter != null)
                        if (nestedIter.hasNext())
                            return true;
                        else
                            nestedIter = null;

                    if (i < array.length) {
                        MapNode node = array[i++];
                        if (node != null)
                            nestedIter = node.iterator();
                    } else
                        return false;
                }
            }

            public Object next() {
                if (hasNext())
                    return nestedIter.next();
                else
                    throw new IllegalStateException();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    final static class BitmapIndexedNode implements MapNode {
        static final BitmapIndexedNode EMPTY = new BitmapIndexedNode(0, new Object[0]);

        int bitmap;
        Object[] array;


        final int index(int bit) {
            return Integer.bitCount(bitmap & (bit - 1));
        }

        BitmapIndexedNode(int bitmap,
                          Object[] array) {
            this.bitmap = bitmap;
            this.array = array;

        }

        public MapNode assoc(int shift, int hash, Object key, Object val,
                             Box addedLeaf) {
            int bit = bitpos(hash, shift);
            int idx = index(bit);
            if ((bitmap & bit) != 0) {
                Object keyOrNull = array[2 * idx];
                Object valOrNode = array[2 * idx + 1];
                if (keyOrNull == null) {
                    MapNode n = ((MapNode) valOrNode).assoc(shift + 5, hash, key,
                            val, addedLeaf);
                    if (n == valOrNode)
                        return this;
                    return new BitmapIndexedNode(bitmap, cloneAndSet(
                            array, 2 * idx + 1, n));
                }
                if (key.equals(keyOrNull)) {
                    if (val == valOrNode)
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
                                val)));
            } else {
                int n = Integer.bitCount(bitmap);
                if (n >= 16) {
                    MapNode[] nodes = new MapNode[32];
                    int jdx = mask(hash, shift);
                    nodes[jdx] = EMPTY.assoc(shift + 5, hash, key, val,
                            addedLeaf);
                    int j = 0;
                    for (int i = 0; i < 32; i++)
                        if (((bitmap >>> i) & 1) != 0) {
                            if (array[j] == null)
                                nodes[i] = (MapNode) array[j + 1];
                            else
                                nodes[i] = EMPTY.assoc(shift + 5,
                                        hash(array[j]), array[j], array[j + 1],
                                        addedLeaf);
                            j += 2;
                        }
                    return new ArrayNode(n + 1, nodes);
                } else {
                    Object[] newArray = new Object[2 * (n + 1)];
                    System.arraycopy(array, 0, newArray, 0, 2 * idx);
                    newArray[2 * idx] = key;
                    addedLeaf.val = addedLeaf;
                    newArray[2 * idx + 1] = val;
                    System.arraycopy(array, 2 * idx, newArray, 2 * (idx + 1),
                            2 * (n - idx));
                    return new BitmapIndexedNode(bitmap | bit, newArray);
                }
            }
        }

        public MapNode without(int shift, int hash, Object key) {
            int bit = bitpos(hash, shift);
            if ((bitmap & bit) == 0)
                return this;
            int idx = index(bit);
            Object keyOrNull = array[2 * idx];
            Object valOrNode = array[2 * idx + 1];
            if (keyOrNull == null) {
                MapNode n = ((MapNode) valOrNode).without(shift + 5, hash, key);
                if (n == valOrNode)
                    return this;
                if (n != null)
                    return new BitmapIndexedNode(bitmap, cloneAndSet(
                            array, 2 * idx + 1, n));
                if (bitmap == bit)
                    return null;
                return new BitmapIndexedNode(bitmap ^ bit, removePair(
                        array, idx));
            }
            if (key.equals(keyOrNull))
                // TODO: collapse
                return new BitmapIndexedNode(bitmap ^ bit, removePair(
                        array, idx));
            return this;
        }

        public PMapEntry find(int shift, int hash, Object key) {
            int bit = bitpos(hash, shift);
            if ((bitmap & bit) == 0)
                return null;
            int idx = index(bit);
            Object keyOrNull = array[2 * idx];
            Object valOrNode = array[2 * idx + 1];
            if (keyOrNull == null)
                return ((MapNode) valOrNode).find(shift + 5, hash, key);
            if (key.equals(keyOrNull))
                return new PMapEntry(keyOrNull, valOrNode);
            return null;
        }

        public Object find(int shift, int hash, Object key, Object notFound) {
            int bit = bitpos(hash, shift);
            if ((bitmap & bit) == 0)
                return notFound;
            int idx = index(bit);
            Object keyOrNull = array[2 * idx];
            Object valOrNode = array[2 * idx + 1];
            if (keyOrNull == null)
                return ((MapNode) valOrNode).find(shift + 5, hash, key, notFound);
            if (key.equals(keyOrNull))
                return valOrNode;
            return notFound;
        }

        /*public ISeq<Object> nodeSeq() {
            return NodeSeq.create(array);
        }*/

        public Iterator<Object> iterator() {
            return new NodeIter(array);
        }


    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    final static class HashCollisionNode implements MapNode {

        final int hash;
        int count;
        Object[] array;

        HashCollisionNode(int hash, int count, Object... array) {
            this.hash = hash;
            this.count = count;
            this.array = array;
        }

        public MapNode assoc(int shift, int hash, Object key, Object val,
                             Box addedLeaf) {
            if (hash == this.hash) {
                int idx = findIndex(key);
                if (idx != -1) {
                    if (array[idx + 1] == val)
                        return this;
                    return new HashCollisionNode(hash, count,
                            cloneAndSet(array, idx + 1, val));
                }
                Object[] newArray = new Object[2 * (count + 1)];
                System.arraycopy(array, 0, newArray, 0, 2 * count);
                newArray[2 * count] = key;
                newArray[2 * count + 1] = val;
                addedLeaf.val = addedLeaf;
                return new HashCollisionNode(hash, count + 1, newArray);
            }
            // nest it in a bitmap node
            return new BitmapIndexedNode(bitpos(this.hash, shift),
                    new Object[]{null, this}).assoc(shift, hash, key, val,
                    addedLeaf);
        }

        public MapNode without(int shift, int hash, Object key) {
            int idx = findIndex(key);
            if (idx == -1)
                return this;
            if (count == 1)
                return null;
            return new HashCollisionNode(hash, count - 1, removePair(
                    array, idx / 2));
        }

        public PMapEntry find(int shift, int hash, Object key) {
            int idx = findIndex(key);
            if (idx < 0)
                return null;
            if (key.equals(array[idx]))
                return new PMapEntry(array[idx], array[idx + 1]);
            return null;
        }

        public Object find(int shift, int hash, Object key, Object notFound) {
            int idx = findIndex(key);
            if (idx < 0)
                return notFound;
            if (key.equals(array[idx]))
                return array[idx + 1];
            return notFound;
        }


        public Iterator iterator() {
            return new NodeIter(array);
        }

        public int findIndex(Object key) {
            for (int i = 0; i < 2 * count; i += 2) {
                if (key.equals(array[i]))
                    return i;
            }
            return -1;
        }
    }

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

    private static Object[] cloneAndSet(Object[] array, int i, Object a, int j,
                                        Object b) {
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

    private static MapNode createNode(int shift, Object key1, Object val1,
                                      int key2hash, Object key2, Object val2) {
        int key1hash = hash(key1);
        if (key1hash == key2hash)
            return new HashCollisionNode(key1hash, 2, new Object[]{
                    key1, val1, key2, val2});
        Box addedLeaf = new Box(null);

        return BitmapIndexedNode.EMPTY.assoc(shift, key1hash, key1, val1,
                addedLeaf).assoc(shift, key2hash, key2, val2, addedLeaf);
    }

    @Override
    public PMap<K, V> distinct() {
        return this;
    }

    private static int bitpos(int hash, int shift) {
        return 1 << mask(hash, shift);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class NodeIter implements Iterator<Object> {

        final Object[] array;
        private int i = 0;
        private Object nextEntry = null;
        private Iterator nextIter;

        NodeIter(Object[] array) {
            this.array = array;
        }

        private boolean advance() {
            while (i < array.length) {
                Object key = array[i];
                Object nodeOrVal = array[i + 1];
                i += 2;
                if (key != null) {
                    nextEntry = new PMapEntry(key == sNullKey ? null : key, nodeOrVal);
                    return true;
                } else if (nodeOrVal != null) {
                    Iterator iter = ((MapNode) nodeOrVal).iterator();
                    if (iter != null && iter.hasNext()) {
                        nextIter = iter;
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean hasNext() {
            if (nextEntry != null || nextIter != null)
                return true;
            return advance();
        }

        public Object next() {
            Object ret = nextEntry;
            if (ret != null) {
                nextEntry = null;
                return ret;
            } else if (nextIter != null) {
                ret = nextIter.next();
                if (!nextIter.hasNext())
                    nextIter = null;
                return ret;
            } else if (advance())
                return next();
            throw new IllegalStateException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(obj instanceof IPMap == false){
            return false;
        }
        IPMap other = (IPMap)obj;
        if(other.size() != size()){
            return false;
        }
        for(Tuple2 entry : this){
            Object v1 = entry._2;
            Object v2 = other.get(entry._1);
            if(v1 == null){
                return v2 == null;
            }
            if(v1.equals(v2) == false){
                return false;
            }
        }
        return true;
    }


}
