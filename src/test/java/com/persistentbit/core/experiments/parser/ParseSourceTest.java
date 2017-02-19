package com.persistentbit.core.experiments.parser;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;

/**
 * TODOC
 *
 * @author petermuys
 * @since 18/02/17
 */
public class ParseSourceTest{

	static final TestCase rollBack = TestCase.name("Source rollbackTest").code(tr -> {
		ParseSource source = ParseSource.asSource("test", "12345679*");
		tr.isEquals(source.current(), (int) '1');
		tr.isEquals(source.getPosition(), new Position("test", 1, 1));
		source = source.next();
		tr.isEquals(source.current(), (int) '2');
		tr.isEquals(source.getPosition(), new Position("test", 1, 2));
		source = source.withSnapshot();
		source = source.next();
		source = source.next();
		tr.isEquals(source.current(), (int) '4');
		tr.isEquals(source.getPosition(), new Position("test", 1, 4));
		source = source.getSnapshot();
		tr.isEquals(source.current(), (int) '2');
		tr.isEquals(source.getPosition(), new Position("test", 1, 2));
		source = source.next();
		tr.isEquals(source.current(), (int) '3');
		tr.isEquals(source.getPosition(), new Position("test", 1, 3));

		source = ParseSource.asSource("test", "12345679*");
		tr.isEquals(source.current(),(int)'1');
		source = source.next(); //2
		tr.isEquals(source.current(),(int)'2');
		source = source.withSnapshot();
		tr.isEquals(source.current(),(int)'2');
		source = source.next(); //3
		tr.isEquals(source.current(),(int)'3');
		source = source.next(); //4
		tr.isEquals(source.current(),(int)'4');
		source = source.withSnapshot();
		tr.isEquals(source.current(),(int)'4');
		source = source.next(); //5
		tr.isEquals(source.current(),(int)'5');
		source = source.next(); //6
		tr.isEquals(source.current(),(int)'6');
		source = source.getSnapshot(); //4
		tr.isEquals(source.current(),(int)'4');
		source = source.getSnapshot(); //2
		tr.isEquals(source.current(),(int)'2');
	});


	public void testAll() {
		TestRunner.runAndPrint(LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)), ParseSourceTest.class);
	}

	public static void main(String[] args) {
		LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();
		new ParseSourceTest().testAll();
	}
}
