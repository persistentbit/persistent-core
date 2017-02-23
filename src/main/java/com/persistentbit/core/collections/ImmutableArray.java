package com.persistentbit.core.collections;

import com.persistentbit.core.OK;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Lightweight wrapper around a Java array([...]) to make it immutable.
 *
 * @author petermuys
 * @since 23/02/17
 */
public class ImmutableArray<T> implements Iterable<T>{

	private final T[] data;

	private ImmutableArray(T[] data) {
		this.data = newArray(data.length, data);
	}

	private ImmutableArray(T[] data, OK noCopy) {
		this.data = data;
	}

	@SafeVarargs
	public static <R> ImmutableArray<R> val(R... data) {
		return new ImmutableArray<>(data);
	}

	public static <R> ImmutableArray<R> from(Iterable<R> iterable) {
		int         length = 0;
		Iterator<R> iter   = iterable.iterator();
		while(iter.hasNext()) {
			length++;
			iter.next();
		}
		return from(iterable, length);
	}

	public static <R> ImmutableArray<R> from(PStream<R> stream) {
		return new ImmutableArray<>(stream.toArray());
	}

	public static <R> ImmutableArray<R> from(Collection<R> collection) {
		return from(collection, collection.size());
	}

	public static <R> ImmutableArray<R> from(Iterable<R> iterable, int length) {
		R[]         newData = newArray(length);
		Iterator<R> iter    = iterable.iterator();
		for(int t = 0; t < length; t++) {
			newData[t] = iter.next();
		}
		return new ImmutableArray<>(newData, OK.inst);
	}

	public int size() {
		return data.length;
	}

	public T get(int index) {
		return data[index];
	}

	public final ImmutableArray<T> plusAll(T... addArray) {
		T[] newData = newArray(data.length + addArray.length, data);
		System.arraycopy(addArray, 0, newData, data.length, addArray.length);
		return new ImmutableArray<T>(newData, OK.inst);
	}

	public final ImmutableArray<T> plus(T item) {
		return plusAll(item);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>(){
			int index;

			@Override
			public boolean hasNext() {
				return index < data.length;
			}

			@Override
			public T next() {
				return data[index++];
			}
		};
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof ImmutableArray)) return false;

		ImmutableArray<?> that = (ImmutableArray<?>) o;

		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		return Arrays.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	/**
	 * Create a copy of a generic typed array
	 *
	 * @param length The length of the new array
	 * @param array  the array to copy
	 * @param <T>    The type of the array
	 *
	 * @return
	 */
	@SafeVarargs
	public static <T> T[] newArray(int length, T... array) {
		return Arrays.copyOf(array, length);
	}
}
