package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.glasgolia.compiler.RStack;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UReflect;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/03/17
 */
public class RVal implements RExpr, RAssignable{

	protected final StrPos pos;
	protected final Class type;
	protected final RStack stack;
	protected final int index;
	private boolean isInitialized;


	public RVal(StrPos pos, Class type, RStack stack, int index) {
		this.pos = pos;
		this.type = type;
		this.stack = stack;
		this.index = index;
	}

	@Override
	public Class getType() {
		return type;
	}

	@Override
	public StrPos getPos() {
		return pos;
	}

	@Override
	public boolean isConst() {
		return isInitialized;
	}

	@Override
	public Object get() {


		return stack.get(index);
	}

	@Override
	public Object assign(Object other) {
		if(isInitialized) {
			throw new EvalException("val is already initialized", pos);
		}
		isInitialized = true;
		stack.set(index, other);
		return other;
	}

	@Override
	public String toString() {
		return "RVal(" + index + "):" + UReflect.present(getType());
	}
}
