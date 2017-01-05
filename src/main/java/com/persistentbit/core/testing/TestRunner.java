package com.persistentbit.core.testing;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.logging.*;
import com.persistentbit.core.result.Result;

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

    public static void runTest(TestCase testCase){
        runTest(LogPrinter.consoleInColor().registerAsGlobalHandler(),testCase);
    }
    public static void runTest(LogPrinter lp, TestCase testCase){
        Result<TestCase> resultCase = getTestRunResult(testCase);

        lp.print(resultCase.getLog());
    }


    public static  Result<TestCase> getTestRunResult(TestCase testCode){

        LogEntryFunction fun = LogEntryFunction.of(testCode.getContext()
            .withTimestamp(System.currentTimeMillis()))
            .withParams("\"" + testCode.getName() + "\"");

        TestRunner tr = new TestRunner(fun);
        try{
            testCode.getTestCode().accept(tr);
            fun = tr.entry.withTimestampDone(System.currentTimeMillis());
            LogEntryFunction finalLog = fun.withResultValue("OK");
            return Result.success(testCode).mapLog(l -> finalLog.append(l));
        }catch(LoggedException le){
            fun = tr.entry.withTimestampDone(System.currentTimeMillis());
            LogEntryFunction finalLog = fun
                .withResultValue("TEST FAILED")
                .append(le.getLogs());
            return Result.<TestCase>failure(le).mapLog(l -> finalLog.append(l));
        }
        catch(Throwable e){
            fun = tr.entry.withTimestampDone(System.currentTimeMillis());
            LogEntryFunction finalLog = fun
                .withResultValue("TEST FAILED");
            return Result.<TestCase>failure(e).mapLog(l -> finalLog.append(l));
        }
    }




    public void assertSuccess(Result<?> res){

        Runnable onError = () -> {

            throw new RuntimeException("Expected Success, got " + res);
        };
        res.ifEmpty(onError);
        res.ifFailure(e -> {
            String msg = "Expected Success, got " + res;
            error(msg);
            throw new RuntimeException(msg,e);
        });
    }
    public void assertEmpty(Result<?> res){
        if(res.isEmpty()){
            return;
        }
        if(res.isPresent()){
            String msg = "Expected Empty, got " + res;
            error(msg);
            throw new RuntimeException(msg);
        }
        res.orElseThrow();
    }
    public void assertFailure(Result<?> res){
        if(res.isError()){
            return;
        }
        throw new RuntimeException("Expected Failure, got " + res);
    }
    public  void assertException(Callable<?> code){
        assertException(code,e -> true);
    }
    public  void assertException(Callable<?> code, Predicate<Exception> verifyException){
        try{
            code.call();
        }catch (Exception e){
            if(verifyException.test(e) == false){
                throw new RuntimeException("Verification of thrown Exception failed.",e);
            }
            return;
        }
        throw new RuntimeException("Expected an exception.");
    }
    public void assertTrue(boolean b){
        assertTrue(b,()->"Expected condition to be true");
    }
    public void assertTrue(boolean b, Supplier<String> error){
        if(b == false) {
            error(error.get());
            throw new RuntimeException(error.get());
        }
    }



}
