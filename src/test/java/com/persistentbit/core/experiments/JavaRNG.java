package com.persistentbit.core.experiments;

import java.util.Random;

/**
 * TODOC
 *
 * @author petermuys
 * @since 23/12/16
 */
public class JavaRNG implements RNG{

	private final long   seed;
	private final Random random;

	private JavaRNG(long seed) {
		this.seed = seed;
		this.random = new Random(seed);
	}

	@Override
	public StateTuple<Integer, RNG> nextInt() {
		return new StateTuple<>(random.nextInt(), this);
	}

	public static RNG rng(long seed) {
		return new JavaRNG(seed);
	}

	@Override
	public String toString() {
		return "JavaRNG(seed =" + seed + ")";
	}
}
