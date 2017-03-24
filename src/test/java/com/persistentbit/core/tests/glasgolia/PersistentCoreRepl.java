package com.persistentbit.core.tests.glasgolia;

import com.persistentbit.core.glasgolia.repl.GlasgoliaRepl;
import com.persistentbit.core.glasgolia.repl.ReplConfig;

/**
 * TODOC
 *
 * @author petermuys
 * @since 6/03/17
 */
public class PersistentCoreRepl {

	public static void main(String[] args) throws Exception {
		ReplConfig config = new ReplConfig().withReplInitResourceName("persistentcore.repl.test");
		GlasgoliaRepl repl = new GlasgoliaRepl(config);
		repl.startRepl();
		//GGRepl repl = new GGRepl()
		//	.loadAndEval("persistentcore.repl.test.gg");
		//repl.repl();
	}
}
