package com.persistentbit.core.collections;


import com.persistentbit.core.tuples.Tuple2;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A PStream is an Iterable object that is persistent (Immutable).<br>
 * There are 2 main types of PStreams. Lazy and Direct PStreams.<br>
 * In Lazy PStreams, nothing is done until the Iterator over this PStream is invoked.<br>
 * In Direct PStream, the actions are done when the method is called.
 * Examples of direct implementations: {@link PList},{@link PSet},{@link PMap} and {@link LList}.
 * Every direct implementation can be converted by calling the {@link #lazy()} method to create a
 * lazy version.
 *
 * @author Peter Muys
 * @see PList
 * @see LList
 * @see PSet
 * @see POrderedSet
 * @see PMap
 * @see POrderedMap
 * @since 6/07/2016
 */
public interface PStream<T> extends Iterable<T>{

	/**
	 * Create a PStream from an Optional value.
	 *
	 * @param opt The optional
	 * @param <T> The type of the PStream
	 *
	 * @return An empty PStream or a Pstream with 1 element
	 */
	@SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
	static <T> PStream<T> from(Optional<T> opt) {
		if(opt.isPresent()) {
			return val(opt.get());
		}
		return val();
	}

	@SafeVarargs
	static <T> PStream<T> val(T... values) {
		return new AbstractPStreamLazy<T>(){
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>(){
					int i;

					@Override
					public boolean hasNext() {
						return i < values.length;
					}

					@Override
					public T next() {
						return values[i++];
					}
				};
			}
		};
	}

	/**
	 * Create a PStream from an {@link Iterator} or a {@link PStreamable}<br>
	 *
	 * @param iter The Iterator or PStreamable
	 * @param <T>  The type of the resulting stream
	 *
	 * @return The PStream
	 */
	@SuppressWarnings("unchecked")
	static <T> PStream<T> from(Iterable<T> iter) {
		if(iter instanceof PStream) {
			return ((PStream<T>) iter);
		}
		if(iter instanceof PStreamable) {
			return ((PStreamable<T>) iter).pstream().lazy();
		}
		if(iter instanceof Collection) {
			Collection col = (Collection) iter;
			Object[]   arr = col.toArray();
			return new AbstractPStreamLazy<T>(){
				@Override
				public Iterator<T> iterator() {
					return new Iterator<T>(){
						int i;

						@Override
						public boolean hasNext() {
							return i < arr.length;
						}

						@Override
						public T next() {
							return (T) arr[i++];
						}
					};
				}
			};
		}
		return PList.<T>empty().plusAll(iter).lazy();
	}

	/**
	 * Get a lazy version of this stream (if it is not already lazy).<br>
	 *
	 * @return A Lazy version of this PStream
	 */
	PStream<T> lazy();

	static <T> PStream<T> from(T[] values) {
		if(values == null) {
			return PList.<T>empty().lazy();
		}

		Object[] fixed = new Object[values.length];
		System.arraycopy(values, 0, fixed, 0, values.length);
		return new AbstractPStreamLazy<T>(){
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>(){
					int i;

					@Override
					public boolean hasNext() {
						return i < fixed.length;
					}

					@SuppressWarnings("unchecked")
					@Override
					public T next() {
						return (T) fixed[i++];
					}
				};
			}
		};
	}

	static PStream<Integer> range(int start, int length) {
		return sequence(start).limit(length);
	}


	static PStream<Integer> sequence(int start) {
		return sequence(start, i -> i + 1);
	}

	static <T> PStream<T> sequence(T start, Function<T, T> next) {
		return new AbstractPStreamLazy<T>(){

			@Override
			public boolean isInfinite() {
				return true;
			}

			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>(){
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

	static PStream<Long> sequence(long start) {
		return sequence(start, i -> i + 1);
	}

	static <T> PStream<T> repeatValue(T value) {
		return new AbstractPStreamLazy<T>(){
			@Override
			public boolean isInfinite() {
				return true;
			}

			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>(){
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


	@SafeVarargs
	static <E> E[] newArray(int length, E... array) { return Arrays.copyOf(array, length); }

	/**
	 * @return true if this stream is infinitely long.
	 */
	boolean isInfinite();


	/**
	 * Create a new Empty PStream with the same implementation.
	 *
	 * @return A new Empty PStream
	 */
	PStream<T> clear();


	/**
	 * Limit this stream to a provided count number of elements
	 *
	 * @param count The maximum number of elements to return in this stream
	 *
	 * @return The limited stream
	 */
	PStream<T> limit(int count);

	PStream<T> until(Predicate<T> until);

	/**
	 * Get a new PStream where the last item is removed from the stream
	 *
	 * @return The new PStream
	 */
	PStream<T> dropLast();

	/**
	 * Create a new PStream where every item is mapped to a new value
	 *
	 * @param mapper The Mapper that transforms a PStream item
	 * @param <R>    The New Type of items
	 *
	 * @return A New stream where every item is mapped.
	 */
	<R> PStream<R> map(Function<? super T, ? extends R> mapper);

	/**
	 * Filter this stream using a Predicate
	 *
	 * @param p The predicate to filter. returning true to include the item in the new filter
	 *
	 * @return The new filtered stream
	 */
	PStream<T> filter(Predicate<? super T> p);

	/**
	 * shortcut for filter(x -&gt; x != null)
	 *
	 * @return a new PStream where all null items are removed.
	 */
	PStream<T> filterNulls();

	/**
	 * Find the first element tested ok
	 *
	 * @param p The predicate to use
	 *
	 * @return An Optional found value
	 */
	Optional<T> find(Predicate<? super T> p);

	/**
	 * Create a new PStream that combines 2 separate streams<br>
	 * Example: <pre>{@code
	 *  PStream.val(1,2,3,4).zip(PStream.val('a','b','c'))
	 *  == PStream.val(Tuple2(1,'a'),Tuple2(2,'b'),Tuple2(3,'c'))
	 * }</pre>
	 * The resulting stream length is the smallest length of the 2 PStreams.
	 *
	 * @param zipStream The stream to zip with
	 * @param <Z>       The type of elements in the second stream
	 *
	 * @return A PStream of Tuple2 values
	 */
	<Z> PStream<Tuple2<Z, T>> zip(PStream<Z> zipStream);

	/**
	 * Create a new PStream that combines the element with the
	 * head/middle/end position of the element in the stream.<br>
	 * The first element of the PStream has a {@link HeadMiddleEnd#head}
	 * position.<br>
	 * All elements between the first and last elements have a {@link HeadMiddleEnd#middle}
	 * position.<br>
	 * The last element has a {@link HeadMiddleEnd#headAndEnd}.<br>
	 * If there is only 1 element in the PStream, then the position is {@link HeadMiddleEnd#headAndEnd}
	 *
	 * @return A new PStream with the HeadMiddleEnd position.
	 */
	PStream<Tuple2<HeadMiddleEnd, T>> headMiddleEnd();

	/**
	 * Zip this stream with a sequence starting with 0.<br>
	 * Handy if you need the index position of an element in a stream
	 *
	 * @return A zipped stream
	 *
	 * @see #zip(PStream)
	 */
	PStream<Tuple2<Integer, T>> zipWithIndex();

	// todo add later
	// <A,B> Tuple2<PStream<A>,PStream<B>> unzip(Function<T,Tuple2<A,B>> unzipper);

	/**
	 * Return an instance of this PStream as a java Stream
	 *
	 * @return A Java Stream instance
	 */
	Stream<T> stream();

	/**
	 * Create a new sorted PStream using the provided {@link Comparator}.<br>
	 * A Lazy stream will likely be converted to a non lazy implementation by this.<br>
	 *
	 * @param comp The comparator to compare the items
	 *
	 * @return A new Sorted PStream
	 */
	PStream<T> sorted(Comparator<? super T> comp);

	/**
	 * Create a sorted PStream by assuming that all items in this stream are {@link Comparable}
	 *
	 * @return A new Sorted PStream
	 *
	 * @see #sorted(Comparator)
	 */
	PStream<T> sorted();

	/**
	 * Reverse the order of all elements in this PStream
	 *
	 * @return The new PStream with the item reversed
	 */
	PStream<T> reversed();

	/**
	 * Check if this PStream contains the provided object
	 *
	 * @param value The value to check
	 *
	 * @return true if this PStream contains the item.
	 */
	boolean contains(Object value);

	/**
	 * Check if this PStream contains all the items in the provide collection.
	 *
	 * @param iter The collections to check
	 *
	 * @return True if this PStream contains all the elements in the provide collection
	 */
	boolean containsAll(Iterable<?> iter);

	/**
	 * Create a new PStream with all elements from this stream that are not in the supplied stream.
	 *
	 * @param others The other stream of elements
	 *
	 * @return PStream with all elements not in others.
	 */
	PStream<T> filterNotContainedIn(PStream<? extends T> others);

	/**
	 * Group the elements in this PStream by generating a key to group on.<br>
	 * This will create a PMap with the generated key and a PList of items for that value.
	 *
	 * @param keyGen The Key generator from a value
	 * @param <K>    The type of the key
	 *
	 * @return The PMap
	 */
	<K> PMap<K, PList<T>> groupBy(Function<? super T, ? extends K> keyGen);

	/**
	 * Same as {@link #groupBy(Function)} but only adding the last value for each key.
	 *
	 * @param keyGen The Key generator from a value
	 * @param <K>    The type of the key
	 *
	 * @return The PMap
	 */
	<K> PMap<K, T> groupByOneValue(Function<? super T, ? extends K> keyGen);

	/**
	 * Group the elements in this PStream by generating a key/value pair for every item and
	 * adding them to a PMap with the generated key and a list of matching generated values<br>
	 *
	 * @param keyGen The Key generator
	 * @param valGen The value generator
	 * @param <K>    The type of the key
	 * @param <V>    The type of the value
	 *
	 * @return The PMap
	 */
	<K, V> PMap<K, PList<V>> groupBy(Function<? super T, ? extends K> keyGen, Function<? super T, ? extends V> valGen);

	/**
	 * Same as {@link #groupBy(Function, Function)} but only adding the last value for each key.
	 *
	 * @param keyGen The Key generator
	 * @param valGen The value generator
	 * @param <K>    The type of the key
	 * @param <V>    The type of the value
	 *
	 * @return The PMap
	 */
	<K, V> PMap<K, V> groupByOneValue(Function<? super T, ? extends K> keyGen, Function<? super T, ? extends V> valGen);

	/**
	 * Same as {@link #groupBy(Function)}, but adding them to a POrderedMap
	 *
	 * @param keyGen The key generator
	 * @param <K>    The type of the key
	 *
	 * @return Th Ordered map
	 */
	<K> POrderedMap<K, PList<T>> groupByOrdered(Function<? super T, ? extends K> keyGen);

	/**
	 * Same as {@link #groupBy(Function, Function)}, but adding them to a POrderedMap
	 *
	 * @param keyGen The key generator
	 * @param valGen The value generator
	 * @param <K>    The type of the key
	 * @param <V>    The type of the value
	 *
	 * @return The Ordered map
	 */
	<K, V> POrderedMap<K, PList<V>> groupByOrdered(Function<? super T, ? extends K> keyGen,
												   Function<? super T, ? extends V> valGen
	);

	/**
	 * Create a new PStream with the provided item added
	 *
	 * @param value The value to add to the end of this PStream
	 *
	 * @return The new PStream with the value added
	 */
	PStream<T> plus(T value);

	/**
	 * Add all items provided add the end of this PStream
	 *
	 * @param iter The Iterable collections of items to add.
	 *
	 * @return A new PStream with the added items.
	 */
	PStream<T> plusAll(Iterable<? extends T> iter);

	/**
	 * Flattened the provide Collection of Collections of items and
	 * add them to the end of this PStream
	 *
	 * @param iterIter The collection of collections of items.
	 *
	 * @return The new PStream with the added items
	 */
	PStream<T> flattenPlusAll(Iterable<Iterable<? extends T>> iterIter);

	/**
	 * Add all the provided items to the end of this PStream
	 *
	 * @param v1   first item to add
	 * @param rest The rest of the items to add
	 *
	 * @return The new PStream with the added items
	 */
	@SuppressWarnings("unchecked")
	PStream<T> plusAll(T v1, T... rest);

	/**
	 * Apply a binary operation to the initial value of this PStream and all the others.<br>
	 * Example:
	 * <pre>{@code
	 *  PStream.val(1,2,3).fold(5,(a,b)-> a+b) == 5+1+2+3
	 * }</pre>
	 *
	 * @param init  The Initial value
	 * @param binOp The binary operation to apply
	 *
	 * @return The end result
	 */
	T fold(T init, BinaryOperator<T> binOp);

	/**
	 * Apply a binary operation to the initial value of this PStream and all the others, starting from the left.<br>
	 * Example:
	 * <pre>{@code
	 *  PStream.val(1, 2, 3).fold("0",s -> i -> "(" + s + " + " +  i + ")")) == (((0 + 1) + 2) + 3)
	 * }</pre>
	 *
	 * @param init The Initial value
	 * @param f    The binary operation to apply
	 * @param <R>  The Resulting type
	 *
	 * @return The end result
	 *
	 * @see #foldRight(Object, Function)
	 */
	<R> R fold(R init, Function<R, Function<T, R>> f);

	/**
	 * Apply a binary operation to the inital value of this PStream and all the others, starting from the right
	 * <pre>{@code
	 *   PStream.val(1, 2, 3).foldRight("0",i -> s -> "(" + i + " + " +  s + ")")) == (1 + (2 + (3 + 0)))
	 * }</pre>
	 *
	 * @param init The Initial value
	 * @param f    The function to apply
	 * @param <R>  The Resulting type
	 *
	 * @return The end result
	 *
	 * @see #fold(Object, Function)
	 */
	<R> R foldRight(R init, Function<T, Function<R, R>> f);


	//TODO implement later
	//T reduce(Function<T, Function<T, T>> f);

	/**
	 * Folds over this PStream using a different initial value and end result.
	 *
	 * @param init  The Initial value
	 * @param binOp The binary operation
	 * @param <X>   The Resulting and initial type
	 *
	 * @return The X value
	 */
	<X> X with(X init, BiFunction<X, T, X> binOp);

	/**
	 * Get the first element of this PStream as an optional
	 *
	 * @return The first optional element
	 */
	Optional<T> headOpt();

	/**
	 * Get the first element of this PStream or null if does not exist.
	 *
	 * @return The first element
	 */
	T head();

	/**
	 * Just cast all elements in this PStream to a new Type.
	 *
	 * @param itemClass The class of the new element type
	 * @param <X>       The new element type
	 *
	 * @return The same stream casted
	 */
	<X> PStream<X> cast(Class<X> itemClass);

	/**
	 * Get the last element of this PStream as an optional
	 *
	 * @return The optional last element in this stream
	 */
	Optional<T> lastOpt();

	/**
	 * Get the element before the last element in this PStream
	 *
	 * @return The Optional element before the last element.
	 */
	Optional<T> beforeLastOpt();

	/**
	 * Create a new PStream where the first occurrence of the provided element
	 * is replaced.
	 *
	 * @param original The original element to find
	 * @param newOne   The new element to replace it with
	 *
	 * @return The new PStream with the replaced element
	 */
	PStream<T> replaceFirst(T original, T newOne);

	/**
	 * Return a new PStream without the first element
	 *
	 * @return The new PStream
	 *
	 * @throws IllegalStateException when the stream was empty
	 */
	PStream<T> tail();

	/**
	 * Get the biggest item in this PStream
	 *
	 * @param comp The {@link Comparator} used to find the biggest item
	 *
	 * @return The biggest item or empty if this PStream is empty
	 */
	Optional<T> max(Comparator<T> comp);

	/**
	 * Get the smallest item int this PStream
	 *
	 * @param comp The {@link Comparator} used to find the smallest item
	 *
	 * @return The smallest item or empty if this PStream is empty
	 */
	Optional<T> min(Comparator<T> comp);

	/**
	 * Same as {@link #min(Comparator)} but using the {@link Comparable} interface of the items
	 *
	 * @return The Smallest item
	 */
	Optional<T> min();

	/**
	 * Same as {@link #max(Comparator)} but using the {@link Comparable} interface of the items
	 *
	 * @return The biggest item
	 */
	Optional<T> max();

	/**
	 * @return true if this PStream contains no items
	 */
	boolean isEmpty();

	/**
	 * @return The number of items in this PStream
	 */
	int size();

	/**
	 * Count the number of items in this PStream conforming the predicate.
	 *
	 * @param predicate The filter for the items to count
	 *
	 * @return The number of items in the PStream
	 */
	int count(Predicate<? super T> predicate);

	/**
	 * Copy all items in this PStream into a new java array
	 *
	 * @return The new array
	 */
	T[] toArray();

	/**
	 * Copy all items in this PStream into the provided array or a new one if the provided is too small.
	 *
	 * @param a    The array to copy it to
	 * @param <T1> The type of the elements
	 *
	 * @return The array
	 */
	<T1> T1[] toArray(T1[] a);

	/**
	 * Convert this PStream to a Persistent list
	 *
	 * @return the PList instance
	 */
	PList<T> plist();

	/**
	 * Convert this PStream to a Persistent Set
	 *
	 * @return the PSet instance
	 */
	PSet<T> pset();

	/**
	 * Convert this PStream to a Persistent Ordered Set
	 *
	 * @return The {@link POrderedSet} instance
	 */
	POrderedSet<T> porderedset();

	/**
	 * Remove all duplicated items from this PStream.
	 *
	 * @return The new PStream without duplicates
	 */
	PStream<T> distinct();

	/**
	 * Return a new PStream containing all
	 * elements that are multiple times in this stream.
	 *
	 * @return A new PStream containing all the duplicated elements.
	 */
	PStream<T> duplicates();

	/**
	 * @return A Persistent Linked List version of this PStream
	 */
	LList<T> llist();

	/**
	 * @return A Immutable java {@link List} version of this PStream.
	 */
	List<T> list();

	/**
	 * @return a new {@link ArrayList} with all the items in this PStream copied.
	 */
	List<T> toList();

	/**
	 * A fold like function where the head of this PStream is the initial value.<br>
	 * If there are no elements in this stream, return empty.<br>
	 * If there is 1 element in this stream, return the element.<br>
	 * If there are more elements, do a reduce.<br>
	 *
	 * @param joiner The binary operation
	 *
	 * @return The result or empty if this PStream is empty
	 */
	Optional<T> join(BinaryOperator<T> joiner);

	/**
	 * Flatten this stream.
	 *
	 * @param <X> The type of the items in the flatten stream
	 *
	 * @return The flatten stream.
	 */
	<X> PStream<X> flatten();

	/**
	 * @return this.mapString("null")
	 *
	 * @see #mapString(String)
	 */
	@SuppressWarnings("JavaDoc")
	PStream<String> mapString();

	/**
	 * map all items to Strings
	 *
	 * @param nullValue the String to use when the item is null
	 *
	 * @return The null PStream
	 */
	PStream<String> mapString(String nullValue);

	/**
	 * @param sep The separator between items
	 *
	 * @return this.toString("", sep,"")
	 *
	 * @see #toString(String, String, String)
	 */
	@SuppressWarnings("JavaDoc")
	String toString(String sep);

	/**
	 * Convert this PStream to a String.<br>
	 * <pre>{@code
	 *  PStream.val(1,2,3).toString("[",":","]") == "[1:2:3]"
	 * }</pre>
	 *
	 * @param left  The prefix String
	 * @param sep   The separator between items
	 * @param right The postfix String
	 *
	 * @return The string
	 */
	String toString(String left, String sep, String right);

	/**
	 * Calls a consumer functions whenever an element from this stream
	 * is returned.<br>
	 * Example for debugging: <br>
	 * <pre>{@code
	 *      PStream.val(1,10,20,4).map(x -> x / 2).peek(System.out::println);
	 * }</pre>
	 *
	 * @param consumer The function to execute for every element returned from this stream
	 *
	 * @return The new PStream
	 */
	PStream<T> peek(Consumer<? super T> consumer);



	enum HeadMiddleEnd{
		head, middle, end, headAndEnd
	}
}
