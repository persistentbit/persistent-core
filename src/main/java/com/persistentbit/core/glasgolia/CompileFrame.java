package com.persistentbit.core.glasgolia;

import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 8/03/17
 */
public interface CompileFrame{

	class NameDef{

		public final StrPos pos;
		public final String name;
		public final boolean isVal;
		public final Class type;

		public NameDef(StrPos pos, String name, boolean isVal, Class type) {
			this.pos = pos;
			this.name = name;
			this.isVal = isVal;
			this.type = type;
		}
	}

	RExpr bind(StrPos pos, String name);

	void addName(NameDef nameDef);

}
