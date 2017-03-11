package com.persistentbit.core.glasgolia.compiler.frames;

import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 10/03/17
 */
public abstract class AbstractCompileFrame implements CompileFrame{

	private Imported imported = null;

	@Override
	public void addImported(Imported imported) {
		if(this.imported == null) {
			this.imported = imported;
		}
		else {
			this.imported = this.imported.addImport(imported);
		}

	}

	protected Optional<RExpr> getFromLocalImported(String name) {
		if(imported == null) {
			return Optional.empty();
		}
		return imported.bind(name);
	}

	@Override
	public Optional<Class> getType(String name) {
		return imported.bind(name).map(r -> r.getType());
	}
}
