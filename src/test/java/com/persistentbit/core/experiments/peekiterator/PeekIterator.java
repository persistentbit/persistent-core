package com.persistentbit.core.experiments.peekiterator;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.experiments.State;
import com.persistentbit.core.experiments.StateTuple;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 9/02/17
 */
public class PeekIterator<T> extends State<CUR<T>, T>{


	public PeekIterator(Function<CUR<T>, StateTuple<T, CUR<T>>> run) {
		super(run);
	}

	public static State<CUR<Integer>, Integer> intPeek = new PeekIterator<>(CUR::next);

	public static void main(String[] args) {
		CUR<Integer>                      cur = IteratorCUR.cur(PStream.val(1, 5, 4, 9, 10).iterator(), 0);
		StateTuple<Integer, CUR<Integer>> s1  = intPeek.apply(cur);
		System.out.println(s1);
		StateTuple<Integer, CUR<Integer>> s2 = intPeek.apply(s1.state);
		StateTuple<Integer, CUR<Integer>> s3 = intPeek.apply(s2.state);
		System.out.println(s1);
		System.out.println(s2);
		System.out.println(s3);

		//StateTuple<Tuple2<Random.Point, Random.Point>, RNG> s1 = rndPointPairGen.apply(JavaRNG.rng(1));
	}
}
