package com.persistentbit.core.glasgolia.compiler;

import com.persistentbit.core.glasgolia.CompileException;
import com.persistentbit.core.glasgolia.compiler.rexpr.RConst;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.ToDo;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class CompileContext{

	private JavaImports javaImports;

	public CompileContext(JavaImports javaImports) {
		this.javaImports = javaImports;
	}

	public CompileContext() {
		this(new JavaImports());
	}

	public CompileContext addImport(String importName) {
		javaImports = javaImports.add(importName);
		return this;
	}

	public CompileContext addVal(String name, RExpr expr) {
		throw new ToDo();
	}

	public CompileContext addVar(String name, RExpr expr) {
		throw new ToDo();
	}

	public RExpr bindName(StrPos pos, String name) {
		Optional<Class> optClass = javaImports.getClass(name);
		if(optClass.isPresent()) {
			return new RConst(pos, Class.class, optClass.get());
		}
		throw new CompileException("Can't bind name " + name, pos);
	}
}
