package com.persistentbit.core.glasgolia.compiler;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.utils.UReflect;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 25/02/17
 */
public class JavaImports{

	private final PSet<String> imports;
	private PMap<String, Class> resolved;

	public JavaImports(PMap<String, Class> resolved, PSet<String> imports) {
		this.resolved = resolved;
		this.imports = imports;
	}

	public JavaImports() {
		this(PMap.empty(), PSet.empty());
	}

	public Optional<Class> getClass(String name) {
		Optional<Class> res = resolved.getOpt(name);
		if(res.isPresent()) {
			return res;
		}
		int i = name.lastIndexOf(".");
		if(i >= 0) {
			//Full classname...
			return UReflect.getClass(name)
						   .map(this::addResolved);
		}
		PStream<Optional<Class>> resSet = imports.lazy()
												 .map(im -> UReflect.getClass(im + "." + name))
												 .filter(Optional::isPresent);
		if(resSet.isEmpty()) {
			return Optional.empty();
		}
		if(resSet.size() > 1) {
			throw new RuntimeException("Multiple classes for name '" + name + "': " + resSet.toString(", "));
		}
		return resSet.head().map(this::addResolved);
	}

	private Class addResolved(Class cls) {
		resolved = resolved.put(cls.getSimpleName(), cls);
		resolved = resolved.put(cls.getName(), cls);
		return cls;
	}

	public JavaImports add(String importName) {
		/*int i = importName.lastIndexOf('.');
		if(i >= 0) {
			getClass(importName).orElseThrow(() -> new RuntimeException("Unknown class: " + importName));
			return this;
		}*/
		if(getClass(importName).isPresent()) {
			return this;
		}
		return new JavaImports(resolved, imports.plus(importName));
	}
}