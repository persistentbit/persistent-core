package com.persistentbit.core.experiments;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/12/16
 */
public class StateTuple<A, S> extends BaseValueClass{

	public final A value;
	public final S state;

	public StateTuple(A value, S state) {
		this.value = value;
		this.state = state;
	}

}
