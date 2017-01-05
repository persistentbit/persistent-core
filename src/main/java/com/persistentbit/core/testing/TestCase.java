package com.persistentbit.core.testing;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.logging.LogContext;
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
    private final LogContext context;
    private final String name;
    private final String info;
    private final Consumer<TestRunner> testCode; 

    private TestCase(String name, String info,LogContext context,Consumer<TestRunner> testCode) {
        this.name = Objects.requireNonNull(name);
        this.info = Objects.requireNonNull(info);
        this.testCode = Objects.requireNonNull(testCode);
        this.context = Objects.requireNonNull(context);
    }

    static public TestCaseWithName name(String name){
        return new TestCaseWithName(Objects.requireNonNull(name));
    }

    public static class TestCaseWithName{
        private String name;
        private String info;
        private LogContext logContext;

        public TestCaseWithName(String name) {
            this.name = name;
            logContext = new LogContext(Thread.currentThread().getStackTrace()[3]);
        }

        public TestCaseWithName info(String...info){
            this.info = PStream.from(info).toString("\r\n");
            return this;
        }
        public TestCase code(Consumer<TestRunner> testCode){
            return new TestCase(name,info == null ? "?" : info,logContext,testCode);
        }
        public TestCase subTestCases(TestCase...testCases){
            return new TestCase(name,info == null ? "?" : info,logContext,tr -> {
                PList<String> failedTestCases = PList.empty();
                for(TestCase tc : testCases){
                    Result<TestCase> rtc = TestRunner.getTestRunResult(tc);
                    tr.add(rtc);
                    if(rtc.isError()){
                        failedTestCases = failedTestCases.plus(tc.getName());
                    }
                }
                if(failedTestCases.isEmpty() == false){
                    throw new RuntimeException(
                        "Sub Tests failed:" + failedTestCases.toString(", ")
                    );
                }
            });
        }

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

    public Consumer<TestRunner> getTestCode() {
        return testCode;
    }
}
