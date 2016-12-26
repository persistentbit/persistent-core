package com.persistentbit.core.experiments;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.function.F;
import com.persistentbit.core.tuples.Tuple2;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 23/12/16
 */
public interface Random<A> extends Function<RNG, Tuple2<A, RNG>>{

	static <A> Random<A> unit(A a) {
		return rng -> Tuple2.of(a, rng);
	}

	static <A, B> Random<B> map(Random<A> s, Function<A, B> mapper) {
		return rng -> {
			Tuple2<A, RNG> result = s.apply(rng);
			return Tuple2.of(mapper.apply(result._1), result._2);
		};
	}

	default <B> Random<B> map(Function<A, B> mapper) {
		return Random.map(this, mapper);
	}


	Random<Integer> intRnd  = RNG::nextInt;
	Random<Boolean> boolRnd = intRnd.map(i -> (i % 2) == 0);

	static <X, B, R> Function<X, R> compose(Function<B, R> f, Function<X, B> g) {
		return x -> f.apply(g.apply(x));
	}

	Function<Integer, Function<Integer, Integer>> add = a -> b -> a + b;


	Function<Integer, Integer> triple = x -> x * 3;
	Function<Integer, Integer> square = x -> x * x;

	static <A, B, C> Function<B, C> partialA(A a, Function<A, Function<B, C>> f) {
		return f.apply(a);
	}

	static <A, B, C> Function<A, C> partialB(B b, Function<A, Function<B, C>> f) {
		return a -> f.apply(a).apply(b);
	}

	static <A, B, C, D> Function<A, Function<B, Function<C, Function<D, String>>>> f() {
		return a -> b -> c -> d -> String.format("%s, %s, %s, %s", a, b, c, d);
	}

	static <A, B, C> Function<A, Function<B, C>> cury2(Function<Tuple2<A, B>, C> f) {
		return a -> b -> f.apply(Tuple2.of(a, b));
	}


	static void main(String... args) {
		PList<Integer>                list     = PList.val(1, 2, 3, 4, 5);
		String                        identity = "0";
		F<String, F<Integer, String>> addSI    = s -> i -> "(" + s + " + " + i + ")";
		System.out.println(list.fold(identity, addSI));
		System.out.println(
			PStream.val(1, 2, 3).foldRight("0", i -> s -> "(" + i + " + " + s + ")"));

		//F<Integer,F<String,String>> addIS = i -> s -> "(" + i + " + " + s + ")";

	}

}
