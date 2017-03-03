package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.glasgolia.compiler.CompileGToR;
import com.persistentbit.core.utils.StrPos;

import java.util.function.Supplier;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class RObjectChild implements RExpr{

	private final StrPos pos;
	private final RExpr parent;
	private final String name;
	private Supplier<Object> childGetter;

	public RObjectChild(StrPos pos, RExpr parent, String name) {
		this.pos = pos;
		this.parent = parent;
		this.name = name;
	}

	@Override
	public Class getType() {
		return Object.class;
	}

	@Override
	public StrPos getPos() {
		return pos;
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public Object get() {
		if(parent.isConst()) {
			if(childGetter == null) {
				childGetter = createChildGetter();
			}
			return childGetter.get();
		}
		return createChildGetter().get();
	}

	private Supplier<Object> createChildGetter() {
		Object parentValue = parent.get();
		if(parentValue == null) {
			return () -> {
				throw new EvalException("Can't get child '" + name + "' from null", pos);
			};
		}
		if(parentValue instanceof Class) {
			Class pcls = (Class) parentValue;
			return CompileGToR.getConstJavaClassChild(pos, pcls, name);
		}
		return CompileGToR.getConstJavaObjectChild(pos, parentValue, name, parent.isConst());
	}
}
