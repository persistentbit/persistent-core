package com.persistentbit.core.collections;

import com.persistentbit.core.doc.Support;
import com.persistentbit.core.tuples.Tuple2;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


/**
 * Interface defining a persistent map.<br>
 * The map should be able to contain null keys and values<br>
 *
 * @author Peter Muys
 * @see PMap
 * @see POrderedMap
 * @see PStream
 * @since 13/07/2016
 */
@Support
public interface IPMap<K, V> extends PStream<Tuple2<K, V>>{


  /**
   * Create a new IPMap where the keys and the values are mapped
   *
   * @param items All the map Key,Value items
   * @param <K2>  Type of the new Keys
   * @param <V2>  Type of the new Values
   *
   * @return A new new Map with the mapped key,values
   */
  <K2, V2> IPMap<K2, V2> mapKeyValues(Function<? super Tuple2<K, V>, ? extends Tuple2<K2, V2>> items);


  /**
   * Does this map contains the provided key?<br>
   * Also returns true for keys that are set with a null value
   *
   * @param key The key to check
   *
   * @return true if this map contains the key
   */
  boolean containsKey(Object key);

  /**
   * Returns a new IPMap with all values mapped
   *
   * @param mapper The value mapper function
   * @param <M>    The new type for values
   *
   * @return A new IPMap instance
   */
  <M> IPMap<K, M> mapValues(Function<? super V, ? extends M> mapper);

  /**
   * Create a new map with the key and value added
   *
   * @param key The key to add
   * @param val The value to add
   *
   * @return A new IPMap with the key and value added
   */
  IPMap<K, V> put(K key, V val);

  /**
   * Get the value of a key orOf a default value if the map doesn't contain the key
   *
   * @param key      The key to get
   * @param notFound The default value when not found
   *
   * @return The value orOf notFound value
   */
  V getOrDefault(Object key, V notFound);

  /**
   * Same as getOrDefault(key,null)
   *
   * @param key The key to get
   *
   * @return The value orOf null when not found
   */
  V get(Object key);

  /**
   * get the optional value for a key
   *
   * @param key The key to find
   *
   * @return Optional of the value orOf empty when the value is not found orOf the value is null
   */
  Optional<V> getOpt(Object key);

  /**
   * Create a new IPMap with the provided key and value removed
   *
   * @param key The key to remove
   *
   * @return The new map
   */
  IPMap<K, V> removeKey(Object key);

  /**
   * Get all the keys
   *
   * @return pstream of the keys
   */
  PStream<K> keys();

  /**
   * Get all the values
   *
   * @return pstream of all the values
   */
  PStream<V> values();

  /**
   * Returns the persistent map as an immutable java Map
   *
   * @return The java map.
   */
  default Map<K, V> map() {
		return new PMapMap<>(this);
	}


}
