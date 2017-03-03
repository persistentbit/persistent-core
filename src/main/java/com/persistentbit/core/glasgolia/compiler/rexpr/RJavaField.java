package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.easyscript.EvalException;
import com.persistentbit.core.utils.StrPos;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class RJavaField implements RExpr{

	private StrPos pos;
	private Field field;
	private Object parentValue;

	public RJavaField(StrPos pos, Field field, Object parentValue) {
		this.pos = pos;
		this.field = field;
		this.parentValue = parentValue;
	}

	@Override
	public Class getType() {
		return field.getType();
	}

	@Override
	public StrPos getPos() {
		return pos;
	}

	@Override
	public Object get() {
		try {
			return field.get(parentValue);
		} catch(IllegalAccessException e) {
			throw new EvalException(pos, "Error getting field " + field, e);
		}
	}

	@Override
	public boolean isConst() {
		return Modifier.isFinal(field.getModifiers());
	}

	@Override
	public String toString() {
		return "RJavaField(" + field + ")";
	}
}
