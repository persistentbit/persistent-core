package com.persistentbit.core.experiments;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.tuples.Tuple2;

/**
 * TODOC
 *
 * @author petermuys
 * @since 23/12/16
 */
public class RNGenerator{

	public static Tuple2<Integer, RNG> integer(RNG rng) {
		return rng.nextInt();
	}

	public static Random<Integer> integer = RNG::nextInt;

	public static Tuple2<Integer, RNG> integer(RNG rng, int limit) {
		Tuple2<Integer, RNG> random = integer(rng);
		return Tuple2.of(Math.abs(random._1) % limit, random._2);
	}

	public static Tuple2<PList<Integer>, RNG> integers(RNG rng, int length) {
		Tuple2<PList<Integer>, RNG> init = Tuple2.of(PList.empty(), rng);
		return PStream.range(0, length).with(init, (l, i) -> {
			RNG                  lrng = l._2;
			Tuple2<Integer, RNG> res  = RNGenerator.integer(l._2);
			return Tuple2.of(l._1.plus(res._1), res._2);
		});
	}
}
