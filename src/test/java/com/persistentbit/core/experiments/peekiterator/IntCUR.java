package com.persistentbit.core.experiments.peekiterator;

import com.persistentbit.core.experiments.StateTuple;

/**
 * TODOC
 *
 * @author petermuys
 * @since 9/02/17
 */
public interface IntCUR{

	StateTuple<Integer, IntCUR> next();
}
