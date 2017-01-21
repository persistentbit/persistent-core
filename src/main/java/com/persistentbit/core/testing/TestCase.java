package com.persistentbit.core.testing;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.logging.entries.LogContext;
import com.persistentbit.core.result.Result;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A TestCase instance represents some Test code with a name and some optional info.<br>
 *
 *
 * @author Peter Muys
 * @since 5/01/2017
 * @see #name(String)
 *
 */
public final class TestCase{

	private final LogContext           context;
	private final String               name;
	private final String               info;
	private final TestCode testCode;

	private TestCase(String name, String info, LogContext context, TestCode testCode) {
		this.name = Objects.requireNonNull(name);
		this.info = Objects.requireNonNull(info);
		this.testCode = Objects.requireNonNull(testCode);
		this.context = Objects.requireNonNull(context);
	}


	public static TestCaseWithName name(String name) {
		return new TestCaseWithName(Objects.requireNonNull(name));
	}

	/**
	 * Create a TestCase with all non-private static TestCase fields in a Class.
	 * @param testClass The Java Class containing the TestCases
	 * @return A new TestCase with all class test cases as sub test cases
	 */
	public static TestCase forTestClass(Class testClass){
		String         name = testClass.getName();
		String         info = "All TestCases in class " + name;
		LogContext     logContext = new LogContext(Thread.currentThread().getStackTrace()[3]).withMethodName("TestCase");
		PStream<TestCase> fields = PStream.from(testClass.getDeclaredFields())
			.filter(f -> Modifier.isStatic(f.getModifiers()))
			.filter(f -> Modifier.isPrivate(f.getModifiers()) == false)
			.filter(f -> TestCase.class.isAssignableFrom(f.getType()))
			.map(f -> Result.noExceptions(() -> {f.setAccessible(true); return f;}).orElseThrow())
			.map(f -> Result.noExceptions(() -> (TestCase)f.get(null)).orElseThrow());
		TestCode testCode = subTestsCode(fields.toArray(new TestCase[0]));
		return new TestCase(name,info,logContext,testCode);
	}

	public static class TestCaseWithVariants<V>{

		private final String                       name;
		private final String                       info;
		private final PList<Supplier<? extends V>> variants;


		public TestCaseWithVariants(String name, String info,
									PList<Supplier<? extends V>> variants
		) {
			this.name = name;
			this.info = info;
			this.variants = variants;
		}

		public TestCaseWithVariants<V> variant(Supplier<? extends V> variant) {
			return new TestCaseWithVariants<>(name, info, variants.plus(variant));
		}

		public TestCase code(Function<V, TestCode> theCode) {
			LogContext logContext =
				new LogContext(Thread.currentThread().getStackTrace()[2]).withMethodName("TestCase With Variants");

			PList<TestCase> subTestCodes = variants.map(sup ->
															new TestCase(name + " with variant ", info == null ? "?" : info, logContext, tr -> {
																V variant = sup.get();
																tr.info("Variant: " + variant);
																theCode.apply(variant).accept(tr);
															})
			);

			TestCode testCode = subTestsCode(subTestCodes.toArray(new TestCase[0]));
			return new TestCase(name, info == null ? "?" : info, logContext, testCode);
		}
	}


	public static class TestCaseWithName{

		private final String     name;
		private       String     info;
		private final LogContext logContext;

		public TestCaseWithName(String name) {
			this.name = name;
			logContext = new LogContext(Thread.currentThread().getStackTrace()[3]).withMethodName("TestCase");
		}

		public TestCaseWithName info(String... info) {
			this.info = PStream.from(info).toString("\r\n");
			return this;
		}

		public TestCase code(TestCode testCode) {
			return new TestCase(name, info == null ? "?" : info, logContext, testCode);
		}

		public <V> TestCaseWithVariants<V> withVariants(Iterable<Supplier<? extends V>> variants) {
			return new TestCaseWithVariants<>(name, info, PStream.from(variants).plist());
		}

		public <V> TestCaseWithVariants<V> withVariants() {
			return withVariants(PList.empty());
		}

		public TestCase subTestCases(TestCase... testCases) {
			return new TestCase(name, info == null ? "?" : info, logContext, subTestsCode(testCases) );
		}

	}

	public static TestCode subTestsCode(TestCase... testCases){
		return tr -> {
			PList<String> failedTestCases = PList.empty();
			for(TestCase tc : testCases) {
				Result<TestCase> rtc = TestRunner.getTestRunResult(tc);
				tr.add(rtc);
				if(rtc.isError()) {
					failedTestCases = failedTestCases.plus(tc.getName());
				}
			}
			if(failedTestCases.isEmpty() == false) {
				throw new TestException(
					"Sub Tests failed:" + failedTestCases.toString(", ")
				);
			}
		};
	}

	public LogContext getContext() {
		return context;
	}

	public String getName() {
		return name;
	}

	public String getInfo() {
		return info;
	}

	public TestCode getTestCode() {
		return testCode;
	}
}
