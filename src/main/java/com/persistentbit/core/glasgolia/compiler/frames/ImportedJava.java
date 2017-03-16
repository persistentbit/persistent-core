package com.persistentbit.core.glasgolia.compiler.frames;

import com.persistentbit.core.glasgolia.compiler.rexpr.RConst;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UReflect;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 10/03/17
 */
public class ImportedJava implements Imported{

	private final String importName;

	public ImportedJava(String importName) {
		this.importName = importName;
	}

	@Override
	public Optional<RExpr> bind(String name) {

		return getJavaClass(name)
					   .map(cls -> new RConst(StrPos.inst, Class.class, cls));
	}

	@Override
	public Optional<Class> getJavaClass(String name) {
		if(name.length() == 0 || Character.isUpperCase(name.charAt(0)) == false){
			return Optional.empty();
		}
		if(importName.endsWith("." + name)) {
			//Must be a class
			return UReflect.getClass(importName).getOpt();
		}

		Result<Class> clsResult = UReflect.getClass(importName + "." + name);
		if(clsResult.isError()) {
			clsResult.orElseThrow();
		}
		return clsResult.getOpt();

	}

	@Override
	public String toString() {
		return "JavaImport(" + importName + ")";
	}
}