package com.persistentbit.core.glasgolia.compiler.frames;

import com.persistentbit.core.glasgolia.compiler.rexpr.GGModule;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 10/03/17
 */
public class ImportedModule implements Imported{

	private final GGModule module;

	public ImportedModule(GGModule module) {
		this.module = module;
	}

	@Override
	public Optional<RExpr> bind(String name) {
		return module.bindChild(name);
	}
}
