package com.persistentbit.core.tests;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogFormatter;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 11/01/2017
 */
public abstract class CoreTest {

    static public LogFormatter testLogFormatter = ModuleCore.createLogFormatter(true);
    static public LogPrint     testLogPrint     = LogPrintStream.sysOut(testLogFormatter);

    static public void runTests(TestCase testCase){
        TestRunner.runAndPrint(testLogPrint, testCase);
    }
    static public void runTests(Class testClass){
        TestRunner.runAndPrint(testLogPrint, testClass);
    }
}
