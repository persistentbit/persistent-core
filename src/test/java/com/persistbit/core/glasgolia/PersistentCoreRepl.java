package com.persistbit.core.glasgolia;

import com.persistentbit.core.glasgolia.repl.GGRepl;

/**
 * TODOC
 *
 * @author petermuys
 * @since 6/03/17
 */
public class PersistentCoreRepl {

	public static void main(String[] args) throws Exception {
		GGRepl repl = new GGRepl()
			.loadAndEval("persistentcore.repl.test.gg");
		repl.repl();
	}
}
