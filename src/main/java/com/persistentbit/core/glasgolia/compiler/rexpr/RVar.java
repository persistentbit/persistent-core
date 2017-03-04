package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.glasgolia.compiler.RStack;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UReflect;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/03/17
 */
public class RVar extends RVal implements RAssignable{

	public RVar(StrPos pos, Class type, RStack stack, int index
	) {
		super(pos, type, stack, index);
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public Object assign(Object other) {
		stack.set(index, other);
		return other;
	}

	@Override
	public String toString() {
		return "RVar(" + index + "):" + UReflect.present(getType());
	}
}
