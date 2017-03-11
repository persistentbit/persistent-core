package com.persistentbit.core.glasgolia.compiler.frames;

import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 10/03/17
 */
public interface Imported{

	Optional<RExpr> bind(String name);

	default Imported addImport(Imported otherImport) {
		return name -> {
			Optional<RExpr> found = otherImport.bind(name);
			if(found.isPresent()) {
				return found;
			}
			return Imported.this.bind(name);
		};
	}
}
