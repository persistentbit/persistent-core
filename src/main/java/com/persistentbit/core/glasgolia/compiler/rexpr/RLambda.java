package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.glasgolia.compiler.RStack;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UReflect;

/**
 * TODOC
 *
 * @author petermuys
 * @since 4/03/17
 */
public class RLambda implements RFunction{

	private final StrPos pos;
	private final int paramCount;
	private final String[] paramNames;
	private final Class[] paramTypes;
	private final Object[] freeVars;
	private final RExpr code;
	private final RStack runtimeStack;


	public RLambda(StrPos pos, int paramCount, String[] paramNames, Class[] paramTypes, Object[] freeVars,
				   RExpr code,
				   RStack runtimeStack
	) {
		this.pos = pos;
		this.paramCount = paramCount;
		this.paramNames = paramNames;
		this.paramTypes = paramTypes;
		this.freeVars = freeVars;
		this.code = code;
		this.runtimeStack = runtimeStack;
	}

	@Override
	public String toString() {
		return "RLambda(paramCount:" + paramCount + ", FreeVars:" + PStream.val(freeVars)
																		   .toString(",") + ", code:" + code + ")";
	}

	public String typeDefToString() {
		String params = PStream.val(paramNames).zip(PStream.val(paramTypes))
							   .map(t -> t._2 + ": " + UReflect.present(t._1)).toString("(", ", ", ")");
		return "lambda " + params + " -> " + UReflect.present(code.getType());
	}

	public Class getResultType(Class[] argTypes) {
		return code.getType();
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
