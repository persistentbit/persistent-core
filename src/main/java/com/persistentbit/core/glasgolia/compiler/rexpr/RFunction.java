package com.persistentbit.core.glasgolia.compiler.rexpr;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public interface RFunction{

	Object apply(Object[] arguments);
	Class getResultType(Class[] argTypes);
}
