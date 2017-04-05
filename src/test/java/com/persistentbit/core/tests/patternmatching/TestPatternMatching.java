package com.persistentbit.core.tests.patternmatching;

import com.persistentbit.core.patternmatching.Matcher;
import com.persistentbit.core.patternmatching.UMatch;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.tests.CoreTest;

import java.util.function.Predicate;

/**
 * Unit tests for package com.persistentbit.core.patternmatching
 *
 * @author petermuys
 * @since 5/04/17
 */
public class TestPatternMatching{

	static final TestCase testPredicates = TestCase.name("Test General Predicates").code(tr -> {
		Predicate predClass = UMatch.classIs(Integer.class);
		tr.isTrue(predClass.test(100));
		tr.isFalse(predClass.test(100.0));
		tr.isFalse(predClass.test(null));

		Predicate predEq = UMatch.isEqual("Hello");
		tr.isTrue(predEq.test("Hello"));
		tr.isFalse(predEq.test("Not Hello"));
		tr.isFalse(predEq.test(null));

		Predicate predAssign = UMatch.isAssignableTo(Number.class);
		tr.isTrue(predAssign.test(100));
		tr.isTrue(predAssign.test(100.0));
		tr.isFalse(predAssign.test(null));
		tr.isFalse(predAssign.test("Not assignable"));
	});

	static final TestCase testMatcher = TestCase.name("Test Matcher").code(tr -> {
		Matcher<Object,String> m = Matcher.defaultCase(v-> "Undefined")
			  .caseClassIs(String.class,root -> v -> "Match string " + v)
			  .caseIsAssignableTo(Number.class, root -> v -> "Match number " + v)
			  .addCase(UMatch.isAssignableTo(Number.class).andForClass(v -> v.intValue() == 42),root -> v -> "Got 42:" + v);
			;
		tr.isEquals(m.match("Hello"),"Match string Hello");
		tr.isEquals(m.match(false),"Undefined");
		tr.isEquals(m.match(42.0),"Got 42:42.0");
		tr.isEquals(m.match(42),"Got 42:42");
	});

	public void testAll() {
		CoreTest.runTests(TestPatternMatching.class);
	}

	public static void main(String... args) throws Exception {
		new TestPatternMatching().testAll();
	}
}
