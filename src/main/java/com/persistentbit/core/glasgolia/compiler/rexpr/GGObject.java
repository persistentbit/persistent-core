package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.exceptions.ToDo;
import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 8/03/17
 */
public interface GGObject{

	Object getChild(StrPos pos, String name);

	Object binOp(StrPos pos, String op, Object other);

	Object castTo(StrPos pos, Class cls);

	default Object assignChild(String childName, Object value){
		throw new ToDo("in " + this.getClass());
	}
}
