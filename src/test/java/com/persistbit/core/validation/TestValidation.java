package com.persistbit.core.validation;

import com.persistbit.core.CoreTest;
import com.persistbit.core.utils.TestValue;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.validation.StringValidator;
import com.persistentbit.core.validation.Validator;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/02/17
 */
public class TestValidation{

	static final TestCase testValidations = TestCase.name("Test Validations").code(tr -> {
		tr.isFalse(Validator.notNull().validate("TestValue", null).isEmpty());

		TestValue tv = new TestValue(1, "Peter");

		tr.isTrue(Validator.notNull().validate("TestValue", tv).isEmpty());
		Validator.<TestValue>notNull()
			.and(v -> v.getName(), "name", StringValidator.minLength(1).and(StringValidator.maxLength(10)))
			.validateToResult("TestValue", tv).orElseThrow();
		tr.isFalse(Validator.<TestValue>notNull()
			.and(v -> v.getName(), "name", StringValidator.minLength(10))
			.validate("TestValue", tv).isEmpty());
		Validator.<TestValue>notNull()
			.and(v -> v.getName(), "name", StringValidator.mustContainAll(true, "et", "er"))
			.validateToResult("TestValue", tv).orElseThrow();
		tr.isFalse(Validator.<TestValue>notNull()
			.and(v -> v.getName(), "name", StringValidator.mustContainAll(true, "et", "er", "a", "p"))
			.validate("TestValue", tv).isEmpty());
	});

	public void testAll() {
		CoreTest.runTests(TestValidation.class);
	}

	public static void main(String... args) throws Exception {
		new TestValidation().testAll();
	}
}
