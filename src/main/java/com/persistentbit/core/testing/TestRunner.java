package com.persistentbit.core.testing;

import com.persistentbit.core.logging.LogPrinter;
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
public class TestRunner {

    private TestRunner() {
    }




    public static void runTest(TestCase testCase){
        runTest(LogPrinter.consoleInColor().registerAsGlobalHandler(),testCase);
    }
    public static void runTest(LogPrinter lp, TestCase testCase){
        Result<TestCase> resultCaste = getTestRunResult(testCase);
        resultCaste.ifPresent( cn -> {
            System.out.println("TEST " + testCase.getName() + " OK");
        });
        resultCaste.ifFailure(e -> {
            System.out.println("TEST " + testCase.getName() + " FAILED");

        });
        lp.print(resultCaste.getLog());
    }


    public static  Result<TestCase> getTestRunResult(TestCase testCode){
        TestRunner tr = new TestRunner();
        return Result.function(testCode.getName(),testCode.getInfo()).code(l -> {
            testCode.getTestCode().accept(tr);
            l.info("Test " + testCode.getName() + " OK");
            return Result.success(testCode);
        });

    }




    public void assertSuccess(Result<?> res){
        Runnable onError = () -> {

            throw new RuntimeException("Expected Success, got " + res);
        };
        res.ifEmpty(onError);
        res.ifFailure(e -> {
            throw new RuntimeException("Expected Success, got " + res,e);
        });
    }
    public void assertEmpty(Result<?> res){
        if(res.isEmpty()){
            return;
        }
        if(res.isPresent()){
            throw new RuntimeException("Expected Empty, got " + res);
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
            throw new RuntimeException(error.get());
        }
    }



}
