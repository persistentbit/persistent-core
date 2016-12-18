package com.persistentbit.core.function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 16/12/16
 */
@FunctionalInterface
public interface Consumer3<P1, P2, P3>{

	void accept(P1 p1, P2 p2, P3 p3);
}
