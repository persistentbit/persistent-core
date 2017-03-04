package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.glasgolia.compiler.RStack;
import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 4/03/17
 */
public class RLambda implements RFunction{

	private final StrPos pos;
	private final int paramCount;
	private final Object[] freeVars;
	private final RExpr code;
	private final RStack runtimeStack;


	public RLambda(StrPos pos, int paramCount,
				   Object[] freeVars,
				   RExpr code, RStack runtimeStack
	) {
		this.pos = pos;
		this.paramCount = paramCount;
		this.freeVars = freeVars;
		this.code = code;
		this.runtimeStack = runtimeStack;
	}

	@Override
	public String toString() {
		return "RLambda(paramCount:" + paramCount + ", FreeVars:" + PStream.val(freeVars)
																		   .toString(",") + ", code:" + code + ")";
	}

	@Override
	public Object apply(Object[] arguments) {
		if(arguments.length != paramCount) {
			throw new EvalException("Expected " + paramCount + " arguments, got " + arguments.length, pos);
		}
		Object[] frame = new Object[paramCount + freeVars.length];
		System.arraycopy(arguments, 0, frame, 0, arguments.length);
		System.arraycopy(freeVars, 0, frame, paramCount, freeVars.length);
		runtimeStack.addFrame(frame);
		try {
			return code.get();
		} finally {
			runtimeStack.popFrame();
		}

	}


}
