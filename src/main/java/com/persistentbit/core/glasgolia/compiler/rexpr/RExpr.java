package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.utils.StrPos;

import java.util.function.Supplier;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public interface RExpr extends Supplier<Object>{

	Class getType();

	StrPos getPos();

	boolean isConst();

	default boolean isAssignableFrom(RExpr other) {
		return other != null && getType().isAssignableFrom(other.getType());
	}
}
