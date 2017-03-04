package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UReflect;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class RConst implements RExpr{

	private final StrPos pos;
	private final Class type;
	private final Object value;

	public RConst(StrPos pos, Class type, Object value) {
		this.pos = pos;
		this.type = type;
		this.value = value;
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
	public Object get() {
		return value;
	}

	@Override
	public String toString() {
		return "RConst(" + value + ")" + ":" + UReflect.present(type);
	}

	@Override
	public boolean isConst() {
		return true;
	}
}
