package com.persistentbit.core.collections;

import com.persistentbit.core.OK;
import com.persistentbit.core.doc.uml.UmlType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Lightweight wrapper around a Java array([...]) to make it immutable.
 *
 * @author petermuys
 * @since 23/02/17
 */
@UmlType
public final class ImmutableArray<T> extends AbstractIPList<T, ImmutableArray<T>> implements Serializable{

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
		for(R anIterable : iterable) {
			length++;
		}
		return from(iterable, length);
	}

	public static <R> ImmutableArray<R> from(PStream<R> stream) {
		return new ImmutableArray<>(stream.toArray());
	}

	public static <R> ImmutableArray<R> from(Collection<R> collection) {
		return from(collection, collection.size());
	}

	public static <R> ImmutableArray<R> from(R[] items, int offset, int length) {
		R[] newArr = newArray(length);
		System.arraycopy(items, offset, newArr, 0, length);
		return new ImmutableArray<>(newArr, OK.inst);
	}

	public static <R> ImmutableArray<R> from(Iterable<R> iterable, int length) {
		R[]         newData = newArray(length);
		Iterator<R> iter    = iterable.iterator();
		for(int t = 0; t < length; t++) {
			newData[t] = iter.next();
		}
		return new ImmutableArray<>(newData, OK.inst);
	}

	@Override
	public int size() {
		return data.length;
	}

	@Override
	public T get(int index) {
		return data[index];
	}

	@SafeVarargs
	@Override
	public final ImmutableArray<T> plusAll(T first, T... addArray) {
		T[] newData = newArray(data.length + addArray.length + 1, data);
		newData[0] = first;
		System.arraycopy(addArray, 0, newData, data.length + 1, addArray.length);
		return new ImmutableArray<>(newData, OK.inst);
	}

	@Override
	public ImmutableArray<T> put(int index, T value) {
		T[] newData = newArray(data.length, data);
		newData[index] = value;
		return new ImmutableArray<>(newData, OK.inst);
	}

	@Override
	public <R> R match(Supplier<R> emptyList, Function<T, R> singleton, Function<IPList<T>, R> headTail
	) {
		if(isEmpty()) {
			return emptyList.get();
		}
		if(size() == 1) {
			return singleton.apply(head());
		}
		return headTail.apply(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ImmutableArray<T> toImpl(PStream<T> lazy) {
		if(lazy instanceof ImmutableArray) {
			return (ImmutableArray) lazy;
		}
		return ImmutableArray.from(lazy);
	}

	@Override
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
	 * @return the java array of T
	 */
	@SafeVarargs
	public static <T> T[] newArray(int length, T... array) {
		return Arrays.copyOf(array, length);
	}
}
