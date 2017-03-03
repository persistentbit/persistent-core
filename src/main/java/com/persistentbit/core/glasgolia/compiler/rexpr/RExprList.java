package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.collections.ImmutableArray;
import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class RExprList implements RExpr{

	private StrPos pos;
	private ImmutableArray<RExpr> list;

	public RExprList(StrPos pos, ImmutableArray<RExpr> list) {
		this.pos = pos;
		this.list = list;
	}

	@Override
	public Class getType() {
		return list.lastOpt().map(e -> e.getType()).orElse(Void.class);
	}

	@Override
	public StrPos getPos() {
		return pos;
	}

	@Override
	public Object get() {
		Object res = null;
		for(RExpr e : list) {
			res = e.get();
		}
		return res;
	}

	@Override
	public String toString() {
		return "" + list.toString("; ") + "";
	}

	@Override
	public boolean isConst() {
		return list.lastOpt().map(e -> e.isConst()).orElse(true);
	}
}
