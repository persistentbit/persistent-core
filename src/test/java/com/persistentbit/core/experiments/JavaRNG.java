package com.persistentbit.core.experiments;

import com.persistentbit.core.tuples.Tuple2;

import java.util.Random;

/**
 * TODOC
 *
 * @author petermuys
 * @since 23/12/16
 */
public class JavaRNG implements RNG{

	private final Random random;

	private JavaRNG(long seed) {
		this.random = new Random(seed);
	}

	@Override
	public Tuple2<Integer, RNG> nextInt() {
		return Tuple2.of(random.nextInt(), this);
	}

	public static RNG rng(long seed) {
		return new JavaRNG(seed);
	}
}
