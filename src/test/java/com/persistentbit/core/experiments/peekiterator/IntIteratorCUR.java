package com.persistentbit.core.experiments.peekiterator;

import com.persistentbit.core.experiments.StateTuple;

import java.util.Iterator;

/**
 * TODOC
 *
 * @author petermuys
 * @since 9/02/17
 */
public class IntIteratorCUR implements IntCUR{

	private final Iterator<Integer> iter;
	private final Integer doneValue;

	private IntIteratorCUR(Iterator<Integer> iter, Integer doneValue) {
		this.iter = iter;
		this.doneValue = doneValue;
	}

	public static IntCUR cur(Iterator<Integer> iter, Integer doneValue) {
		return new IntIteratorCUR(iter, doneValue);
	}


	@Override
	public StateTuple<Integer, IntCUR> next() {
		if(iter.hasNext() == false) {
			return new StateTuple<>(doneValue, this);
		}
		return new StateTuple<>(iter.next(), this);
	}


	@Override
	public String toString() {
		return "IteratorCUR(" + iter + ")";
	}
}
