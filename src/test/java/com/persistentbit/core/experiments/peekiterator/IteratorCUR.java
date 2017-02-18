package com.persistentbit.core.experiments.peekiterator;

import com.persistentbit.core.experiments.StateTuple;

import java.util.Iterator;

/**
 * TODOC
 *
 * @author petermuys
 * @since 9/02/17
 */
public class IteratorCUR<T> implements CUR<T>{

	private final Iterator<T> iter;
	private final T doneValue;

	private IteratorCUR(Iterator<T> iter, T doneValue) {
		this.iter = iter;
		this.doneValue = doneValue;
	}

	public static <T> CUR<T> cur(Iterator<T> iter, T doneValue) {
		return new IteratorCUR<>(iter, doneValue);
	}


	@Override
	public StateTuple<T, CUR<T>> next() {
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
