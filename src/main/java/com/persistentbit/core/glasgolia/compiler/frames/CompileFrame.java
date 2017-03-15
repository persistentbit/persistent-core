package com.persistentbit.core.glasgolia.compiler.frames;

import com.persistentbit.core.glasgolia.compiler.rexpr.GGAccess;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.utils.StrPos;

import java.util.Optional;

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
		public final GGAccess access;
		public final Class type;

		public NameDef(StrPos pos, String name, boolean isVal, GGAccess access, Class type) {
			this.pos = pos;
			this.name = name;
			this.isVal = isVal;
			this.type = type;
			this.access = access;
		}

		public NameDef withIsVal(boolean isVal) {
			return new NameDef(pos, name, isVal, access, type);
		}

		public NameDef withType(Class type) {
			return new NameDef(pos, name, isVal, access, type);
		}
	}


	boolean canDefineLocal(String name);

	RExpr bind(StrPos pos, String name);

	void addName(NameDef nameDef);

	int	createStackVarIndex();

	void addImported(Imported imported);

	Optional<Class> getClassForTypeName(String name);
}
