package com.persistbit.core;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.testing.TestCase;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/01/17
 */
public class TestTestCase{


	static final TestCase withVariants = TestCase.name("Check test case variants")
		.withVariants(PList.val(() -> 1.0, () -> 2.0, () -> 3.0)).code(i -> tr -> {
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
