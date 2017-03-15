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
	private final String[] paramNames;
	private final Class[] paramTypes;
	private final PList<Tuple2<Integer, RExpr>> freeInitList;
	private final int frameSize;
	private final RExpr code;
	private final RStack runtimeStack;

	public RLambdaCreate(StrPos pos, int paramCount, String[] paramNames, Class[] paramTypes,
						 PList<Tuple2<Integer, RExpr>> freeInitList,
						 int frameSize,
						 RExpr code,
						 RStack runtimeStack
	) {
		this.pos = pos;
		this.paramCount = paramCount;
		this.paramNames = paramNames;
		this.paramTypes = paramTypes;
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
		return new RLambda(pos, paramCount, paramNames, paramTypes, free, code, runtimeStack);
	}
}
