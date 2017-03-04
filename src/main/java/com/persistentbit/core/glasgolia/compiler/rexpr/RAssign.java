package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/03/17
 */
public class RAssign implements RExpr{

	private RAssignable left;
	private RExpr right;

	public RAssign(RAssignable left, RExpr right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public Class getType() {
		return left.getType();
	}

	@Override
	public StrPos getPos() {
		return left.getPos();
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public Object get() {
		return left.assign(right.get());
	}

	@Override
	public String toString() {
		return "RAssign(" + left + " = " + right + ")";
	}
}
