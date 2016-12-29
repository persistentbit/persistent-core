package com.persistentbit.core.experiments;

/**
 * TODOC
 *
 * @author petermuys
 * @since 23/12/16
 */
public interface RNG{

	StateTuple<Integer, RNG> nextInt();
}
