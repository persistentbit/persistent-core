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

	Optional<Class> getJavaClass(String name);


	default Imported addImport(Imported otherImport) {
		Imported self = this;
		return new Imported() {
			@Override
			public Optional<RExpr> bind(String name) {
				Optional<RExpr> found = self.bind(name);
				if(found.isPresent()) {
					return found;
				}
				return otherImport.bind(name);
			}

			@Override
			public Optional<Class> getJavaClass(String name) {
				Optional<Class> found = self.getJavaClass(name);
				if(found.isPresent()) {
					return found;
				}
				return otherImport.getJavaClass(name);
			}

			@Override
			public String toString() {
				return self + " orTry " + otherImport;
			}
		};

	}
}
