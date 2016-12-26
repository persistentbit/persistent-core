package com.persistentbit.core.experiments;

import com.persistentbit.core.tuples.Tuple2;

/**
 * TODOC
 *
 * @author petermuys
 * @since 23/12/16
 */
public interface RNG{

	Tuple2<Integer, RNG> nextInt();
}
