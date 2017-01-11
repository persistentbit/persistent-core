package com.persistentbit.core.experiments;

import com.persistbit.core.CoreTest;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.logging.entries.LogEntryEmpty;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.testing.TestCase;


/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 5/01/2017
 */

public class ResultTests {
    static  final TestCase testFailureCaseOfResult = TestCase
            .name("Result test failure")
            .info("Check failere case of result")
            .code(t -> {
                t.isFailure(Result.failure("This does not work"));
            });
    static  final TestCase exceptionFailTest = TestCase
            .name("ExceptionFailTest")
            .info("If an exception is thrown during a test, then the test must fail")
            .code(t -> {
                t.throwsException(()-> Result.empty().orElseThrow());
            });
    static  final TestCase logEntryEmptyAddEmpty = TestCase
            .name("LogEntryEmptyAddEmpty")
            .info("if an empty log entry is added to an empty log entry, then the result must be empty")
            .code(t -> {
                LogEntry le = LogEntryEmpty.inst;
                le = le.append(le);
                t.isTrue(le instanceof LogEntryEmpty);
            });

    static  final  TestCase all = TestCase
            .name("All Tests")
            .subTestCases(
                    exceptionFailTest,
                    logEntryEmptyAddEmpty,
                    testFailureCaseOfResult
                    );


    public void testAll() {
        CoreTest.runTests(ResultTests.class);
    }

    public static void main(String... args) throws Exception {
        new ResultTests().testAll();
    }
}
