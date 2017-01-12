package com.persistbit.core;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogPrinter;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 11/01/2017
 */
public abstract class CoreTest {
    static public LogPrinter testLogPrinter = ModuleCore.createLogPrinter(true);

    static public void runTests(TestCase testCase){
        TestRunner.runAndPrint(testLogPrinter,testCase);
    }
    static public void runTests(Class testClass){
        TestRunner.runAndPrint(testLogPrinter,testClass);
    }
}
