package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 4/03/17
 */
public class RWhile implements RExpr{

	private final RExpr cond;
	private final RExpr code;

	public RWhile(RExpr cond, RExpr code) {
		this.cond = cond;
		this.code = code;
	}

	@Override
	public Class getType() {
		return code.getType();
	}

	@Override
	public StrPos getPos() {
		return cond.getPos();
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public Object get() {
		Object result = null;
		while(true) {
			Object condValue = cond.get();
			if(condValue instanceof Boolean == false) {
				throw new EvalException("Expected a boolean condition for while, got:" + condValue, cond.getPos());
			}
			if((Boolean) condValue == false) {
				return result;
			}
			result = code.get();
		}
	}
}
