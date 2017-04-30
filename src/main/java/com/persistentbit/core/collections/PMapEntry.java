package com.persistentbit.core.collections;


import com.persistentbit.core.doc.annotations.DUsedByClass;
import com.persistentbit.core.tuples.Tuple2;

import java.util.Map;

/**
 * User: petermuys
 * Date: 8/07/16
 * Time: 09:19
 */
@DUsedByClass(PMap.class)
@DUsedByClass(POrderedMap.class)
class PMapEntry<K, V> extends Tuple2<K, V> implements Map.Entry<K, V>{

  public PMapEntry(K key, V val) {
	super(key, val);
  }


  @Override
  public K getKey() {
	return _1;
  }

  @Override
  public V getValue() {
	return _2;
  }

  @Override
  public V setValue(V value) {
	throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
	return "(" + _1 + " -> " + _2 + ")";
  }


}
