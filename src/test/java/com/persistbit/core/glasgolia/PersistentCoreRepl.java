package com.persistbit.core.glasgolia;

import com.persistentbit.core.glasgolia.ERepl;

/**
 * TODOC
 *
 * @author petermuys
 * @since 6/03/17
 */
public class PersistentCoreRepl extends ERepl{

	public static void main(String[] args) throws Exception {
		ERepl repl = new ERepl()
			.loadAndEval("persistentcore.repl.test.gg");
		repl.repl();
	}
}
