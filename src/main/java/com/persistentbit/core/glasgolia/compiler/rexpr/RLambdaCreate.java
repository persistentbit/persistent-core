package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.glasgolia.compiler.RStack;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 4/03/17
 */
public class RLambdaCreate implements RExpr{

	private final StrPos pos;
	private final int paramCount;
	private final PList<Tuple2<Integer, RExpr>> freeInitList;
	private final int frameSize;
	private final RExpr code;
	private final RStack runtimeStack;

	public RLambdaCreate(StrPos pos, int paramCount,
						 PList<Tuple2<Integer, RExpr>> freeInitList,
						 int frameSize,
						 RExpr code,
						 RStack runtimeStack
	) {
		this.pos = pos;
		this.paramCount = paramCount;
		this.freeInitList = freeInitList;
		this.frameSize = frameSize;
		this.code = code;
		this.runtimeStack = runtimeStack;
	}

	@Override
	public String toString() {
		return "RLambdaCreate(paramCount:" + paramCount + ", FreeVars:" + freeInitList
			.toString(",") + ", code:" + code + ")";
	}

	/*@Override
	public Object apply(Object[] arguments) {
		System.out.println(runtimeStack.printToString());
		if(arguments.length != paramCount){
			throw new EvalException("Expected " + paramCount + " arguments for lambda ",pos);
		}
		Object[] frame = new Object[paramCount + freeInitList.size()];

		freeInitList.forEach(t -> frame[t._1] = t._2.get());

		for(int t=0; t<arguments.length;t++){
			frame[t] = arguments[t];
		}
		System.out.println("applying lambda with frame " + PStream.val(frame).toString(",") + ": " + code);
		runtimeStack.addFrame(frame);
		System.out.println(runtimeStack.printToString());
		try {
			Object res = code.get();
			System.out.println("apply result for " + this + " = " + res);
			return res;
		}finally {
			runtimeStack.popFrame();
		}
	}*/

	@Override
	public Class getType() {
		return RFunction.class;
	}

	@Override
	public StrPos getPos() {
		return pos;
	}

	@Override
	public boolean isConst() {
		return true;
	}

	@Override
	public Object get() {
		Object[] free = new Object[frameSize - paramCount];
		freeInitList.forEach(t -> free[t._1 - paramCount] = t._2.get());
		return new RLambda(pos, paramCount, free, code, runtimeStack);
	}
}
