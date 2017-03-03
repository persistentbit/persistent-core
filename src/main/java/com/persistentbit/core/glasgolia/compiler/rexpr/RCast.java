package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.glasgolia.compiler.JavaObjectMatcher;
import com.persistentbit.core.utils.StrPos;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class RCast implements RExpr{

	public RExpr expr;
	public Class cls;

	public RCast(RExpr expr, Class cls) {
		this.expr = expr;
		this.cls = cls;
	}

	@Override
	public Class getType() {
		return cls;
	}

	@Override
	public StrPos getPos() {
		return expr.getPos();
	}

	@Override
	public boolean isConst() {
		return expr.isConst();
	}

	@Override
	public Object get() {
		Object           parentValue = expr.get();
		Optional<Object> casted      = JavaObjectMatcher.tryCast(parentValue, cls);
		if(casted.isPresent()) {
			return casted.get();
		}
		throw new EvalException("Can't cast to " + cls.getName() + ":" + parentValue, expr.getPos());
	}
}
