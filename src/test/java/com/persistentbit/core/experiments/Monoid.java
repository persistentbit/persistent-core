package com.persistentbit.core.experiments;

/**
 * TODOC
 *
 * @author petermuys
 * @since 27/12/16
 */
public interface Monoid<A>{
	A zero();
	Monoid<A> append(Monoid<A> aMonoid);
}
