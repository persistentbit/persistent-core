package com.persistentbit.core.testing;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.logging.*;
import com.persistentbit.core.result.Result;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 5/01/2017
 */
public class TestRunner extends LogEntryLogging{

	private LogEntryFunction entry;

	private TestRunner(LogEntryFunction entry) {
		super(2);
		this.entry = entry;
	}

	@Override
	public Nothing add(LogEntry logEntry) {
		entry = entry.append(logEntry);
		return Nothing.inst;
	}

	public static Result<TestCase> runTest(TestCase testCase) {
		return runTest(LogPrinter.consoleInColor().registerAsGlobalHandler(), testCase);
	}

	public static Result<TestCase> runTest(LogPrinter lp, TestCase testCase) {
		Result<TestCase> resultCase = getTestRunResult(testCase);

		lp.print(resultCase.getLog());
		return resultCase;
	}


	public static Result<TestCase> getTestRunResult(TestCase testCode) {

		LogEntryFunction fun = LogEntryFunction.of(testCode.getContext()
													   .withTimestamp(System.currentTimeMillis()))
			.withParams("\"" + testCode.getName() + "\"");

		TestRunner tr = new TestRunner(fun);
		try {
			testCode.getTestCode().accept(tr);
			fun = tr.entry.withTimestampDone(System.currentTimeMillis());
			LogEntryFunction finalLog = fun.withResultValue("OK");
			return Result.success(testCode).mapLog(l -> finalLog.append(l));
		} catch(LoggedException le) {
			fun = tr.entry.withTimestampDone(System.currentTimeMillis());
			LogEntryFunction finalLog = fun
				.withResultValue("TEST FAILED")
				.append(le.getLogs())
				.append(new LogEntryException(le));
			return Result.<TestCase>failure(le).mapLog(l -> finalLog.append(l));
		} catch(Throwable e) {
			fun = tr.entry.withTimestampDone(System.currentTimeMillis());
			LogEntryFunction finalLog = fun
				.withResultValue("TEST FAILED")
				.append(new LogEntryException(e));
			return Result.<TestCase>failure(e).mapLog(l -> finalLog.append(l));
		}
	}


	public void assertSuccess(Result<?> res) {

		if(res.isPresent() == false){
			throw new TestException("Expected Success, got " + res,0);
		}
	}

	public void assertEmpty(Result<?> res) {
		if(res.isEmpty()) {
			return;
		}
		throw new TestException("Expected Empty, got " + res);
	}

	public void assertFailure(Result<?> res) {
		if(res.isError()) {
			return;
		}
		throw new TestException("Expected Failure, got " + res);
	}

	public void assertException(Callable<?> code) {
		assertException(code, e -> true);
	}

	public void assertException(Callable<?> code, Predicate<Exception> verifyException) {
		try {
			code.call();
		} catch(Exception e) {
			if(verifyException.test(e) == false) {
				throw new TestException("Verification of thrown Exception failed.", e);
			}
			return;
		}
		throw new TestException("Expected an exception.");
	}

	public void assertEquals(Object left, Object right) {
		if(Objects.equals(left,right)){
			return;
		}
		throw new TestException("Objects are not equal:" + left + " != right");
	}

	public <T> T runNoException(Callable<T> code) {

		try {
			return code.call();
		} catch(Exception e) {
			throw new TestException("Unexpected exception :-)", e);
		}
	}

	public void assertTrue(boolean b) {
		if(b == false) {
			throw new TestException("Expected condition to be true");
		}
	}

	public void assertTrue(boolean b, Supplier<String> error) {
		if(b == false) {
			throw new TestException(error.get());
		}
	}


}
