package com.persistbit.core.glasgolia;

import com.persistbit.core.CoreTest;
import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.glasgolia.compiler.GlasgoliaCompiler;
import com.persistentbit.core.testing.TestCase;

/**
 * TODOC
 *
 * @author petermuys
 * @since 11/03/17
 */
public class TestGlasgoliaCompiler{

	static final TestCase testModules = TestCase.name("Test Modules").code(tr -> {
		GlasgoliaCompiler gc = GlasgoliaCompiler.replCompiler();
		tr.info(gc.compileCode("import module 'gg.compilertest';").orElseThrow().get());
		tr.info(gc.compileCode("modulevar;").orElseThrow().get());
		tr.info(gc.compileCode("modulevar2").orElseThrow().get());
		tr.info(gc.compileCode("id(100)").orElseThrow().get());
		tr.info(gc.compileCode("add(100)(23)").orElseThrow().get());
		tr.info(gc.compileCode("mul(2,add(3)(id(2)))").orElseThrow().get());
	});

	public void testAll() {
		CoreTest.runTests(TestGlasgoliaCompiler.class);
	}

	public static void main(String[] args) {
		ModuleCore.consoleLogPrint.registerAsGlobalHandler();
		new TestGlasgoliaCompiler().testAll();
	}
}
