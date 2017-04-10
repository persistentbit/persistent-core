package com.persistentbit.core.tests.parser;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;
import com.persistentbit.core.utils.UOS;

/**
 * TODOC
 *
 * @author petermuys
 * @since 18/02/17
 */
public class ParseSourceTest{

	static final TestCase rollBack = TestCase.name("Source rollbackTest").code(tr -> {

		Source source = Source.asSource("test", "1234567890");
		tr.isEquals(source.current, '1');
		source = source.next();
		tr.isEquals(source.current, '2');
		source = source.next();
		tr.isEquals(source.current, '3');
		Source s3 = source;
		source = source.next();
		tr.isEquals(source.current, '4');
		source = source.next();
		tr.isEquals(source.current, '5');
		source = source.next();
		tr.isEquals(source.current, '6');
		source = source.next();
		tr.isEquals(source.current, '7');
		Source s7 = source;
		source = source.next();
		tr.isEquals(source.current, '8');
		source = source.next();
		tr.isEquals(source.current, '9');
		source = source.next();
		tr.isEquals(source.current, '0');
		source = source.next();
		tr.isEquals(source.current, Source.EOF);
		source = source.next();
		tr.isEquals(source.current, Source.EOF);

		tr.isEquals(s3.current, '3');
		s3 = s3.next();
		tr.isEquals(s3.current, '4');

		tr.isEquals(s7.current, '7');
		s7 = s7.next();
		tr.isEquals(s7.current, '8');
		s7 = s7.next();
		tr.isEquals(s7.current, '9');
		s7 = s7.next();
		tr.isEquals(s7.current, '0');
		s7 = s7.next();
		tr.isEquals(s7.current, Source.EOF);


		/*
		tr.isEquals(source.current, (int) '1');
		tr.isEquals(source.getPosition(), new Position("test", 1, 1));
		source = source.next();
		tr.isEquals(source.current, (int) '2');
		tr.isEquals(source.getPosition(), new Position("test", 1, 2));
		source = source.withSnapshot();
		source = source.next();
		source = source.next();
		tr.isEquals(source.current, (int) '4');
		tr.isEquals(source.getPosition(), new Position("test", 1, 4));
		source = source.getSnapshot();
		tr.isEquals(source.current, (int) '2');
		tr.isEquals(source.getPosition(), new Position("test", 1, 2));
		source = source.next();
		tr.isEquals(source.current, (int) '3');
		tr.isEquals(source.getPosition(), new Position("test", 1, 3));

		source = ParseSource.asSource("test", "12345679*");
		tr.isEquals(source.current,(int)'1');
		source = source.next(); //2
		tr.isEquals(source.current,(int)'2');
		source = source.withSnapshot();
		tr.isEquals(source.current,(int)'2');
		source = source.next(); //3
		tr.isEquals(source.current,(int)'3');
		source = source.next(); //4
		tr.isEquals(source.current,(int)'4');
		source = source.withSnapshot();
		tr.isEquals(source.current,(int)'4');
		source = source.next(); //5
		tr.isEquals(source.current,(int)'5');
		source = source.next(); //6
		tr.isEquals(source.current,(int)'6');
		source = source.getSnapshot(); //4
		tr.isEquals(source.current,(int)'4');
		source = source.getSnapshot(); //2
		tr.isEquals(source.current,(int)'2');*/
	});


	public void testAll() {
		TestRunner.runAndPrint(LogPrintStream.sysOut(ModuleCore.createLogFormatter(UOS.hasAnsiColor())), ParseSourceTest.class);
	}

	public static void main(String[] args) {
		LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(UOS.hasAnsiColor())).registerAsGlobalHandler();
		new ParseSourceTest().testAll();
	}
}
