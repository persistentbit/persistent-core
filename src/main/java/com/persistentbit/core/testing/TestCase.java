package com.persistentbit.core.testing;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.logging.LogEntry;
import com.persistentbit.core.logging.LogEntryEmpty;
import com.persistentbit.core.logging.LoggedException;
import com.persistentbit.core.result.Result;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 5/01/2017
 */
public class TestCase{
    private final String name;
    private final String info;
    private final Consumer<TestRunner> testCode; 

    private TestCase(String name, String info,Consumer<TestRunner> testCode) {
        this.name = Objects.requireNonNull(name);
        this.info = Objects.requireNonNull(info);
        this.testCode = Objects.requireNonNull(testCode);
    }

    static public TestCaseWithName name(String name){
        return new TestCaseWithName(Objects.requireNonNull(name));
    }

    public static class TestCaseWithName{
        private String name;
        private String info;

        public TestCaseWithName(String name) {
            this.name = name;
        }

        public TestCaseWithName info(String...info){
            this.info = PStream.from(info).toString("\r\n");
            return this;
        }
        public TestCase code(Consumer<TestRunner> testCode){
            return new TestCase(name,info == null ? "?" : info,testCode);
        }
        public TestCase subTestCases(TestCase...testCases){
            return new TestCase(name,info == null ? "?" : info,tr -> {
                PStream<Result<TestCase>> subResults = PStream
                        .from(testCases)
                        .map(tc -> TestRunner.getTestRunResult(tc)).plist();
                LogEntry allLogs = subResults
                        .map(sr -> sr.getLog())
                        .fold(LogEntryEmpty.inst, (a,b)-> a.append(b));
                boolean hasErrors = subResults.find(sr -> sr.isError()).isPresent();
                if(hasErrors){
                    throw new LoggedException("Errors in sub test cases", allLogs);
                }
            });
        }

    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public Consumer<TestRunner> getTestCode() {
        return testCode;
    }
}
