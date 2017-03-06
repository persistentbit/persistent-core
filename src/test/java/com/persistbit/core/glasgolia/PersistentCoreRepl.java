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
		es.loadAndEval("repl.gg").throwOnError().orElse(null);
		es.loadAndEval("persistentcore.repl.test.gg").throwOnError().orElse(null);
		ERepl.repl();
	}
}
