package com.persistbit.core;

import com.persistentbit.core.testing.TestCase;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/01/17
 */
public class TestTestCase{


	static final TestCase withVariants = TestCase.name("Check test case variants")
		.withVariants(() -> 1, () -> 2, () -> 3).code(i -> tr -> {
			tr.info("Variant " + i);
		});

	public void testAll() {
		CoreTest.runTests(TestTestCase.class);
	}

	public static void main(String[] args) {
		CoreTest.testLogPrint.registerAsGlobalHandler();
		new TestTestCase().testAll();
	}
}
