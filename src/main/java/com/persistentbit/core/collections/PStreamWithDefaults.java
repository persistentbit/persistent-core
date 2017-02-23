package com.persistentbit.core.collections;

import com.persistentbit.core.tuples.Tuple2;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * User: petermuys
 * Date: 20/07/16
 * Time: 17:42
 */
@SuppressWarnings({"InterfaceMayBeAnnotatedFunctional", "unchecked"})
public interface PStreamWithDefaults<T> extends PStream<T>{


	@Override
	default PStream<T> lazy() {
		return this;
	}

	@Override
	default PStream<T> clear() {
		return new AbstractPStreamLazy<T>(){
			@Override
			public Iterator<T> iterator() {
				return Collections.emptyIterator();
			}

		};
	}

	@Override
	default PStream<T> limit(int count) {
		if(count < 0) {
			throw new IndexOutOfBoundsException("count can't be < 0: " + count);
		}
		return new AbstractPStreamLazy<T>(){


			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>(){
					final Iterator<T> master = PStreamWithDefaults.this.iterator();
					int cnt = count;

					@Override
					public boolean hasNext() {
						return cnt > 0 && master.hasNext();
					}

					@Override
					public T next() {
						if(cnt <= 0) {
							throw new IllegalStateException("Over limit");
						}
						cnt--;
						return master.next();
					}
				};
			}
		};
	}


	@Override
	default PStream<T> until(Predicate<T> until) {
		return new AbstractPStreamLazy<T>(){
			@Override
			public boolean isInfinite() {
				return false;
			}

			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>(){
					final Iterator<T> master = PStreamWithDefaults.this.iterator();
					T lastValue;
					boolean gotNext;

					@Override
					public boolean hasNext() {
						gotNext = master.hasNext();
						if(gotNext) {
							lastValue = master.next();
						}
						gotNext = gotNext && until.test(lastValue);
						return gotNext;
					}

					@Override
					public T next() {
						if(gotNext == false) {
							throw new IllegalStateException("Over limit");
						}
						return lastValue;
					}
				};
			}
		};
	}

	@Override
	default PStream<T> filterNotContainedIn(PStream<? extends T> others) {
		PSet otherSet = others.pset();
		return filter(v -> otherSet.contains(v) == false);
	}

	@Override
	default PStream<T> filter(Predicate<? super T> p) {
		return new AbstractPStreamLazy<T>(){
			@Override
			public boolean isInfinite() {
				return PStreamWithDefaults.this.isInfinite();
			}

			@Override
			public Iterator<T> iterator() {
				return new FilteredIterator<>(p, PStreamWithDefaults.this.iterator());
			}


		};

	}

	@Override
	default boolean isInfinite() {
		return false;
	}

	@Override
	default PStream<T> filterNulls() {
		return filter(Objects::nonNull);
	}

	@Override
	default Optional<T> find(Predicate<? super T> p) {
		for(T v : this) {
			if(p.test(v)) {
				return Optional.ofNullable(v);
			}
		}
		return Optional.empty();


	}

	@Override
	default PStream<Tuple2<HeadMiddleEnd, T>> headMiddleEnd() {
		return new AbstractPStreamLazy<Tuple2<HeadMiddleEnd, T>>(){
			@Override
			public boolean isInfinite() {
				return PStreamWithDefaults.this.isInfinite();
			}

			@Override
			public Iterator<Tuple2<HeadMiddleEnd, T>> iterator() {
				Iterator<T> it = PStreamWithDefaults.this.iterator();
				return new Iterator<Tuple2<HeadMiddleEnd, T>>(){
					private HeadMiddleEnd current;

					@Override
					public boolean hasNext() {
						return it.hasNext();
					}

					@Override
					public Tuple2<HeadMiddleEnd, T> next() {
						T result = it.next();
						if(current == null) {
							current = HeadMiddleEnd.head;
							if(it.hasNext() == false) {
								current = HeadMiddleEnd.headAndEnd;
							}
						}
						else {
							if(it.hasNext()) {
								current = HeadMiddleEnd.middle;
							}
							else {
								current = HeadMiddleEnd.end;
							}
						}
						return new Tuple2<>(current, result);
					}
				};
			}
		};
	}

	@Override
	default PStream<Tuple2<Integer, T>> zipWithIndex() {
		return zip(PStream.sequence(0));
	}

	@Override
	default <Z> PStream<Tuple2<Z, T>> zip(PStream<Z> zipStream) {
		return new AbstractPStreamLazy<Tuple2<Z, T>>(){
			@Override
			public boolean isInfinite() {
				return PStreamWithDefaults.this.isInfinite() && zipStream.isInfinite();
			}

			@Override
			public Iterator<Tuple2<Z, T>> iterator() {
				Iterator<Z> iz = zipStream.iterator();
				Iterator<T> it = PStreamWithDefaults.this.iterator();
				return new Iterator<Tuple2<Z, T>>(){

					@Override
					public boolean hasNext() {
						return iz.hasNext() && it.hasNext();
					}

					@Override
					public Tuple2<Z, T> next() {
						return new Tuple2<>(iz.next(), it.next());
					}
				};
			}


		};
	}

	@Override
	default Stream<T> stream() {
		return list().stream();
	}

	@Override
	default List<T> list() {
		if(isInfinite()) { throw new InfinitePStreamException();}


		return plist().list();
	}

	@Override
	default PList<T> plist() {
		if(isInfinite()) { throw new InfinitePStreamException();}

		return new PList<T>().plusAll(this);
	}

	@Override
	default PStream<T> sorted() {
		return sorted((a, b) -> ((Comparable) a).compareTo(b));
	}

	@Override
	default PStream<T> sorted(Comparator<? super T> comp) {
		if(isInfinite()) { throw new InfinitePStreamException(); }
		return new AbstractPStreamLazy<T>(){
			private List<T> sorted;

			@Override
			public synchronized Iterator<T> iterator() {
				if(sorted == null) {
					sorted = new ArrayList<>();
					for(T t : PStreamWithDefaults.this) {
						sorted.add(t);
					}
					sorted.sort(comp);
				}
				return sorted.iterator();
			}


		};
	}

	@Override
	default PStream<T> plusAll(Iterable<? extends T> iter) {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		return new PStreamAnd<>(this, (PStream<T>) PStream.from(iter));
	}

	@Override
	default PStream<T> flattenPlusAll(Iterable<Iterable<? extends T>> iterIter) {

		return PStream.from(iterIter).with((PStream<T>) this, PStream::plusAll);
	}

	@Override
	default boolean contains(Object value) {
		for(T v : this) {
			if(v == null) {
				if(value == null) {
					return true;
				}
			}
			else if(v.equals(value)) {
				return true;
			}

		}
		return false;
	}

	@Override
	default boolean containsAll(Iterable<?> iter) {
		PSet<T> set = this.pset();
		for(Object v : iter) {
			if(set.contains(v) == false) {
				return false;
			}
		}
		return true;
	}

	@Override
	default PSet<T> pset() {
		if(isInfinite()) { throw new InfinitePStreamException();}

		return new PSet<T>().plusAll(this);
	}

	@Override
	default <K> PMap<K, T> groupByOneValue(Function<? super T, ? extends K> keyGen) {
		return (PMap<K, T>) groupBy(keyGen).mapValues(PStreamWithDefaults::head);
	}

	@Override
	default <K> PMap<K, PList<T>> groupBy(Function<? super T, ? extends K> keyGen) {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		PMap<K, PList<T>> r         = PMap.empty();
		PList<T>          emptyList = PList.empty();
		for(T v : this) {
			K        k = keyGen.apply(v);
			PList<T> l = r.getOrDefault(k, emptyList);
			l = l.plus(v);
			r = r.put(k, l);
		}
		return r;
	}

	@Override
	default <K, V> PMap<K, V> groupByOneValue(Function<? super T, ? extends K> keyGen,
											  Function<? super T, ? extends V> valGen
	) {
		return (PMap<K, V>) groupBy(keyGen, valGen).mapValues(l -> l.headOpt().orElse(null));
	}

	@Override
	default <K, V> PMap<K, PList<V>> groupBy(Function<? super T, ? extends K> keyGen,
											 Function<? super T, ? extends V> valGen
	) {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		PMap<K, PList<V>> r         = PMap.empty();
		PList<V>          emptyList = PList.empty();
		for(T v : this) {
			K        k = keyGen.apply(v);
			PList<V> l = r.getOrDefault(k, emptyList);
			l = l.plus(valGen.apply(v));
			r = r.put(k, l);
		}
		return r;
	}

	@Override
	default Optional<T> headOpt() {
		Iterator<T> iter = iterator();
		if(iter.hasNext()) {
			return Optional.ofNullable(iter.next());
		}
		return Optional.empty();
	}

	@Override
	default <K> POrderedMap<K, PList<T>> groupByOrdered(Function<? super T, ? extends K> keyGen) {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		POrderedMap<K, PList<T>> r         = POrderedMap.empty();
		PList<T>                 emptyList = PList.empty();
		for(T v : this) {
			K        k = keyGen.apply(v);
			PList<T> l = r.getOrDefault(k, emptyList);
			l = l.plus(v);
			r = r.put(k, l);
		}
		return r;
	}

	@Override
	default <K, V> POrderedMap<K, PList<V>> groupByOrdered(Function<? super T, ? extends K> keyGen,
														   Function<? super T, ? extends V> valGen
	) {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		POrderedMap<K, PList<V>> r         = POrderedMap.empty();
		PList<V>                 emptyList = PList.empty();
		for(T v : this) {
			K        k = keyGen.apply(v);
			PList<V> l = r.getOrDefault(k, emptyList);
			l = l.plus(valGen.apply(v));
			r = r.put(k, l);
		}
		return r;
	}

	@Override
	default <X> X with(X init, BiFunction<X, T, X> binOp) {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		X res = init;
		for(T v : this) {
			res = binOp.apply(res, v);
		}
		return res;
	}

	@Override
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	default T head() {
		return headOpt().get();
	}

	@Override
	default <X> PStream<X> cast(Class<X> itemClass) {
		return this.map(c -> (X) c);
	}

	@Override
	default <R> PStream<R> map(Function<? super T, ? extends R> mapper) {

		return new AbstractPStreamLazy<R>(){
			@Override
			public boolean isInfinite() {
				return PStreamWithDefaults.this.isInfinite();
			}

			@Override
			public Iterator<R> iterator() {

				return new Iterator<R>(){
					Iterator<T> master;

					@Override
					public boolean hasNext() {
						if(master == null) {
							master = PStreamWithDefaults.this.iterator();
						}
						return master.hasNext();
					}

					@Override
					public R next() {
						return mapper.apply(master.next());
					}
				};
			}

		};
	}

	@Override
	default Optional<T> lastOpt() {
		Iterator<T> iter = iterator();
		T           last = null;
		while(iter.hasNext()) {
			last = iter.next();
		}
		return Optional.ofNullable(last);
	}

	@Override
	default Optional<T> beforeLastOpt() {
		return dropLast().lastOpt();
	}

	@Override
	default PStream<T> dropLast() {
		if(isInfinite()) { throw new InfinitePStreamException();}

		return new AbstractPStreamLazy<T>(){

			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>(){
					final Iterator<T> master = PStreamWithDefaults.this.iterator();
					boolean hasValue = master.hasNext();
					T value = (hasValue ? master.next() : null);

					@Override
					public boolean hasNext() {
						return master.hasNext();
					}

					@Override
					public T next() {
						T res = value;
						hasValue = master.hasNext();
						value = (hasValue ? master.next() : null);
						return res;
					}
				};
			}

		};
	}

	@Override
	default PStream<T> replaceFirst(T original, T newOne) {
		return new AbstractPStreamLazy<T>(){
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>(){
					boolean found;
					Iterator<T> master;

					@Override
					public boolean hasNext() {
						if(master == null) {
							master = PStreamWithDefaults.this.iterator();
						}
						return master.hasNext();
					}

					@Override
					public T next() {
						T v = master.next();
						if(original.equals(v)) {
							v = newOne;
							found = true;
						}
						return v;
					}
				};
			}
		};
	}

	@Override
	default PStream<T> tail() {
		if(isEmpty()) {
			throw new IllegalStateException("Tail of empty stream");
		}
		return new AbstractPStreamLazy<T>(){
			@Override
			public boolean isInfinite() {
				return PStreamWithDefaults.this.isInfinite();
			}

			@Override
			public Iterator<T> iterator() {
				Iterator<T> iter = PStreamWithDefaults.this.iterator();
				if(iter.hasNext()) {
					iter.next();
				}
				return iter;
			}
		};
	}

	@Override
	default boolean isEmpty() {
		return iterator().hasNext() == false;
	}

	@Override
	default Optional<T> min() {
		return min((a, b) -> ((Comparable) a).compareTo(b));
	}

	@Override
	default Optional<T> min(Comparator<T> comp) {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		return headOpt().map(h -> fold(h, (a, b) -> comp.compare(a, b) <= 0 ? a : b));
	}

	@Override
	default <R> R fold(R init, Function<R, Function<T, R>> f) {
		if(isInfinite()) { throw new InfinitePStreamException(); }
		R res = init;
		for(T v : this) {
			res = f.apply(res).apply(v);
		}
		return res;
	}

	@Override
	default <R> R foldRight(R init, Function<T, Function<R, R>> f) {
		if(isInfinite()) { throw new InfinitePStreamException(); }
		R res = init;
		for(T v : this.reversed()) {
			res = f.apply(v).apply(res);
		}
		return res;
	}

	@Override
	default T fold(T init, BinaryOperator<T> binOp) {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		T res = init;
		for(T v : this) {
			res = binOp.apply(res, v);
		}
		return res;
	}

	@Override
	default Optional<T> max() {
		return max((a, b) -> ((Comparable) a).compareTo(b));
	}

	@Override
	default Optional<T> max(Comparator<T> comp) {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		return headOpt().map(h -> fold(h, (a, b) -> comp.compare(a, b) >= 0 ? a : b));
	}

	@Override
	default int count(Predicate<? super T> predicate) {
		return filter(predicate).size();
	}

	@Override
	default PStream<T> plusAll(T v1, T... rest) {
		if(isInfinite()) { throw new InfinitePStreamException();}

		return plus(v1).plusAll(Arrays.asList(rest));
	}

	@Override
	default PStream<T> plus(T value) {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		return new AbstractPStreamLazy<T>(){

			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>(){
					final Iterator<T> master = PStreamWithDefaults.this.iterator();
					boolean valueAdded;

					@Override
					public boolean hasNext() {
						return master.hasNext() || (valueAdded == false);
					}

					@Override
					public T next() {
						if(master.hasNext()) {
							return master.next();
						}
						if(valueAdded == false) {
							valueAdded = true;
							return value;
						}
						throw new IllegalStateException();
					}
				};
			}

		};
	}

	@Override
	default T[] toArray() {
		if(isInfinite()) { throw new InfinitePStreamException();}

		T[] arr = newArray(size());
		int i   = 0;
		for(T v : this) {
			arr[i++] = v;
		}
		return arr;
	}

	static <E> E[] newArray(int length, E... array) { return Arrays.copyOf(array, length); }

	@Override
	default int size() {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		int count = 0;
		for(T t : this) {
			count++;
		}
		return count;
	}

	@Override
	default <T1> T1[] toArray(T1[] a) {
		if(isInfinite()) { throw new InfinitePStreamException();}

		int size = size();
		if(a.length < size) {
			a = Arrays.copyOf(a, size);
		}
		Iterator<T> iter = iterator();
		for(int t = 0; t < a.length; t++) {
			if(iter.hasNext()) {
				a[t] = (T1) iter.next();
			}
			else {
				a[t] = null;
			}
		}
		return a;
	}

	@Override
	default POrderedSet<T> porderedset() {
		if(isInfinite()) { throw new InfinitePStreamException();}

		return new POrderedSet<T>().plusAll(this);
	}

	@Override
	default PStream<T> distinct() {
		return new AbstractPStreamLazy<T>(){
			@Override
			public Iterator<T> iterator() {
				Set<T> lookup = new HashSet<>();
				Predicate<T> distinct = v -> {
					if(lookup.contains(v)) {
						return false;
					}
					lookup.add(v);
					return true;
				};
				return new FilteredIterator<>(distinct, PStreamWithDefaults.this.iterator());
			}

		};
	}

	@Override
	default PStream<T> duplicates() {
		if(isInfinite()) { throw new InfinitePStreamException();}
		return new AbstractPStreamLazy<T>(){
			@Override
			public Iterator<T> iterator() {
				Set<T>         lookup = new HashSet<>();
				POrderedSet<T> dup    = POrderedSet.empty();
				for(T item : PStreamWithDefaults.this) {
					if(lookup.contains(item)) {
						dup = dup.plus(item);
					}
					lookup.add(item);
				}
				return dup.iterator();
			}
		};
	}


	@Override
	default LList<T> llist() {
		if(isInfinite()) { throw new InfinitePStreamException();}


		LList<T> res = LList.empty();
		for(T v : reversed()) {
			res = res.prepend(v);
		}
		return res;
	}

	@Override
	default PStream<T> reversed() {
		if(isInfinite()) { throw new InfinitePStreamException(); }

		return new PStreamReversed<>(this);
	}

	@Override
	default List<T> toList() {
		if(isInfinite()) { throw new InfinitePStreamException();}

		return new ArrayList<>(this.list());
	}


	@Override
	default Optional<T> join(BinaryOperator<T> joiner) {
		if(isInfinite()) { throw new InfinitePStreamException();}


		Iterator<T> iter = iterator();
		if(iter.hasNext() == false) {
			return Optional.empty();
		}
		T res = iter.next();
		while(iter.hasNext()) {
			T sec = iter.next();
			res = joiner.apply(res, sec);
		}
		return Optional.ofNullable(res);
	}

	@Override
	default <X> PStream<X> flatten() {
		return new AbstractPStreamLazy<X>(){
			@Override
			public Iterator<X> iterator() {
				return new FlattenIterator<>(PStreamWithDefaults.this.iterator());
			}
		};
	}

	@Override
	default String toString(String sep) {
		return toString("", sep, "");
	}

	@Override
	default String toString(String left, String sep, String right) {
		return left + mapString().join((a, b) -> a + sep + b).orElse("") + right;
	}

	@Override
	default PStream<String> mapString() {
		return mapString("null");
	}

	@Override
	default PStream<String> mapString(String nullValue) {
		return map(t -> t == null ? nullValue : t.toString());
	}

	@Override
	default PStream<T> peek(Consumer<? super T> consumer) {
		return new AbstractPStreamLazy<T>(){
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>(){
					Iterator<T> master;

					@Override
					public boolean hasNext() {
						if(master == null) {
							master = PStreamWithDefaults.this.iterator();
						}
						return master.hasNext();
					}

					@Override
					public T next() {
						if(master == null) {
							master = PStreamWithDefaults.this.iterator();
						}
						T element = master.next();
						consumer.accept(element);
						return element;
					}
				};
			}

		};
	}

	@Override
	default PStream<T> limitOnPreviousValue(Predicate<T> stopOnCondition) {
		return new AbstractPStreamLazy<T>(){
			@Override
			public boolean isInfinite() {
				return false;
			}

			@Override
			public Iterator<T> iterator() {

				return new Iterator<T>(){
					Iterator<T> master;
					T prevValue;
					boolean hasNext = true;

					private void init() {
						master = PStreamWithDefaults.this.iterator();
						hasNext = master.hasNext();
					}

					@Override
					public boolean hasNext() {
						if(master == null) {
							init();
						}
						else {
							hasNext = hasNext && (stopOnCondition.test(prevValue) == false);
							hasNext = hasNext && master.hasNext();
						}

						return hasNext;
					}

					@Override
					public T next() {
						if(master == null) {
							init();
						}
						if(hasNext == false) {
							throw new IllegalStateException("There is no next value");
						}
						prevValue = master.next();
						return prevValue;
					}
				};
			}

		};
	}

	@Override
	default <A, B> Tuple2<PStream<A>, PStream<B>> unzip(Function<T, Tuple2<A, B>> unzipper) {
		return Tuple2.of(
				map(t -> unzipper.apply(t)._1),
				map(t -> unzipper.apply(t)._2)
		);
	}

	@Override
	default ImmutableArray<T> toImmutableArray() {
		return ImmutableArray.from(this);
	}
}
