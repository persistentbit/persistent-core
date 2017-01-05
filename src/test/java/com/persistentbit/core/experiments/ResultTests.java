package com.persistentbit.core.experiments;

import com.persistentbit.core.logging.LogEntry;
import com.persistentbit.core.logging.LogEntryEmpty;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;
import org.junit.Test;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 5/01/2017
 */

public class ResultTests {
    static public final TestCase testFailureCaseOfResult = TestCase
            .name("Result test failure")
            .info("Check failere case of result")
            .code(t -> {
                t.assertFailure(Result.failure("This does not work"));
            });
    static public final TestCase exceptionFailTest = TestCase
            .name("ExceptionFailTest")
            .info("If an exception is thrown during a test, then the test must fail")
            .code(t -> {
                t.assertException(()-> Result.empty().orElseThrow());
            });
    static public final TestCase logEntryEmptyAddEmpty = TestCase
            .name("LogEntryEmptyAddEmpty")
            .info("if an empty log entry is added to an empty log entry, then the result must be empty")
            .code(t -> {
                LogEntry le = LogEntryEmpty.inst;
                le = le.append(le);
                t.assertTrue(le instanceof  LogEntryEmpty == false);
            });

    static public final  TestCase all = TestCase
            .name("All Tests")
            .subTestCases(
                    testFailureCaseOfResult,
                    exceptionFailTest,
                    logEntryEmptyAddEmpty
            );


    @Test
    public void testAll() {
        TestRunner.runTest(all);
    }

    public static void main(String... args) throws Exception {
        TestRunner.runTest(all);
    }
}
