package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 4/03/17
 */
public class RIf implements RExpr{

	private final RExpr cond;
	private final RExpr codeTrue;
	private final RExpr codeFalse;

	public RIf(RExpr cond, RExpr codeTrue, RExpr codeFalse) {
		this.cond = cond;
		this.codeTrue = codeTrue;
		this.codeFalse = codeFalse;
	}

	@Override
	public Class getType() {
		return codeTrue.getType();
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
		Object condVal = cond.get();
		if(condVal instanceof Boolean == false){
			throw new EvalException("Expected boolean condition for if, got: " + condVal,cond.getPos());
		}
		if((Boolean)condVal){
			return codeTrue.get();
		}
		if(codeFalse != null){
			return codeFalse.get();
		}
		return null;
	}
}
