package com.persistentbit.core.function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 16/12/16
 */
@FunctionalInterface
public interface Consumer2<P1, P2>{

	void accept(P1 p1, P2 p2);
}
