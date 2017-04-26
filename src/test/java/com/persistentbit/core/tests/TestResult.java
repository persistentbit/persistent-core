package com.persistentbit.core.tests;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.logging.entries.LogMessageLevel;
import com.persistentbit.core.logging.entries.LogContext;
import com.persistentbit.core.logging.entries.LogEntryMessage;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestCode;
import com.persistentbit.core.testing.TestRunner;

import java.util.function.Function;

/**
 *
 *
 * @author petermuys
 * @since 6/01/17
 */
public class TestResult{
	private static final Result<Nothing> successResult = Result.success(Nothing.inst);
	private static final Result<Nothing> failureResult = Result.failure("FAILURE");
	private static final Result<Nothing> emptyResult   = Result.empty("EMPTY");

	private static final Result<Nothing> successResultAsync = Result.async(() -> successResult);
	private static final Result<Nothing> failureResultAsync = Result.async(() -> failureResult);
	private static final Result<Nothing> emptyResultAsync   = Result.async(() -> emptyResult);

	private static final Result<Nothing> successResultLazy = Result.lazy(() -> successResult);
	private static final Result<Nothing> failureResultLazy = Result.lazy(() -> failureResult);
	private static final Result<Nothing> emptyResultLazy   = Result.lazy(() -> emptyResult);

	static final TestCase testMap = TestCase.name("Result map function").code(tr -> {
		testMap(tr, successResult,failureResult,emptyResult);
		testMap(tr, successResultAsync, failureResultAsync, emptyResultAsync);
		testMap(tr, successResultLazy, failureResultLazy, emptyResultLazy);
	});



	static final TestCase testEquals = TestCase.name("Result equals/hashcode").code(testTr -> {
		Function<Result<Nothing>, TestCode> tc = success -> tr -> {
			//success is equal if values are equal. Logs doesn't matter
			tr.isEquals(success.map(t-> true),success.map(t->true));
			tr.isEquals(
				success.map(t-> true),
				success.map(t->true).
					mapLog(l -> l.append(LogEntryMessage.of(LogMessageLevel.info,new LogContext(Thread.currentThread().getStackTrace()[0]),"test")))
			);
		};
		tc.apply(successResult).accept(testTr);
		tc.apply(successResultAsync).accept(testTr);
		tc.apply(successResultLazy).accept(testTr);

	});

	static final TestCase testFlatMap = TestCase.name("FlatMap").code(tr -> {
		testFlatMap(tr, successResult,failureResult,emptyResult);
		testFlatMap(tr, successResultAsync, failureResultAsync, emptyResultAsync);
		testFlatMap(tr, successResultLazy, failureResultLazy, emptyResultLazy);
	});

	static final TestCase testGetOpt = TestCase.name("getOpt function").code(tr -> {
		testGetOpt(tr, successResult,failureResult,emptyResult);
		testGetOpt(tr, successResultAsync, failureResultAsync, emptyResultAsync);
		testGetOpt(tr, successResultLazy, failureResultLazy, emptyResultLazy);
	});

	static final TestCase testFlatMapError = TestCase.name("flatMapFailure/flatMapEmpty").code(tr -> {
		tr.warning("TODO test flatMapFailure/flatMapEmpty");
	});

	private static void testGetOpt(TestRunner tr, Result<Nothing> success, Result<Nothing> failure, Result<Nothing> empty){
		tr.isTrue(success.getOpt().isPresent());
		tr.isFalse(failure.getOpt().isPresent());
		tr.isFalse(empty.getOpt().isPresent());
	}

	private static void testFlatMap(TestRunner tr, Result<Nothing> success, Result<Nothing> failure, Result<Nothing> empty){
		//null argument should return a failure
		tr.isFailure(success.flatMap(null));

		//exception during mapping should return a failure
		tr.isFailure(success.flatMap(v -> { throw new RuntimeException("error");}));

		//returning null after mapping should return a failure
		tr.isFailure(success.flatMap(v -> null));

		//mapping in empty or failures should do nothing
		tr.isEmpty(empty.flatMap(null));
		tr.isFailure(failure.flatMap(null));

		//map should change the value in success
		tr.isTrue(success.flatMap(v -> Result.success(true)).orElseThrow());

		//mapping a failure should still return a failure
		tr.isFailure(failure.flatMap(v -> Result.success(true)));

		//mapping an empty should still return an empty
		tr.isEmpty(empty.flatMap(v -> Result.success(true)));
	}
	private static void testMap(TestRunner tr, Result<Nothing> success, Result<Nothing> failure, Result<Nothing> empty){
		//null argument should return a failure
		tr.isFailure(success.map(null));

		//exception during mapping should return a failure
		tr.isFailure(success.map(v -> { throw new RuntimeException("error");}));

		//returning null after mapping should return an empty
		tr.isEmpty(success.map(v -> null));

		//mapping in empty or failures should do nothing
		tr.isEmpty(empty.map(null));
		tr.isFailure(failure.map(null));

		//map should change the value in success
		tr.isTrue(success.map(v -> true).orElseThrow());

		//mapping should not change the logs
		tr.isEquals(success.map(v -> true).getLog(),success.getLog());

		//mapping a failure should still return a failure
		tr.isFailure(failure.map(v -> true));

		//mapping an empty should still return an empty
		tr.isEmpty(empty.map(v -> true));
	}



	public void testAll(){
		CoreTest.runTests(TestResult.class);
	}

	public static void main(String[] args) {
		new TestResult().testAll();
	}
}
