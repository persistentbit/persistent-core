package com.persistentbit.core.glasgolia;

import com.persistentbit.core.Lazy;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.glasgolia.gexpr.GExpr;
import com.persistentbit.core.utils.ToDo;

import java.util.Iterator;
import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/03/17
 */
public class GTypeInference{

	private final Iterator<String> nextUniqueName =
		PStream.sequence('α', prev -> (char) (prev + 1)).map(c -> Character.toString(c)).iterator();
	private final Iterator<Integer> nextUniqueId = PStream.sequence(1).iterator();


	static public class Env{

		private final PMap<String, ETypeSig> map;

		public Env(PMap<String, ETypeSig> map) {
			this.map = map;
		}
	}

	public abstract class Type{

		public class Variable extends Type{

			private final int id;
			public Optional<Type> instance = Optional.empty();
			public final Lazy<String> name = new Lazy<>(() -> nextUniqueName.next());

			public Variable() {
				this.id = nextUniqueId.next();
			}
		}

	}

	public ETypeSig analyse(GExpr ast, Env env) {
		return analyse(ast, env, PSet.empty());
	}

	private ETypeSig analyse(GExpr ast, Env env, PSet<Type.Variable> nongen) {
		throw new ToDo();
	}
}
