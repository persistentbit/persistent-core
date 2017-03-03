package com.persistentbit.core.glasgolia;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.glasgolia.gexpr.GExprParser;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.printing.PrintableText;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/03/17
 */
public class GExprTest{

	public GExprTest(String name) {
		System.out.println("GExprTest " + name);
	}

	public GExprTest() {

	}

	private static void print(String code) {
		GExprParser    parser = new GExprParser();
		ParseResult<?> result = parser.parseExprList().parse(Source.asSource("test", code));
		System.out.println(code);
		System.out
			.println(PrintableText.indent(PrintableText.fromString(result.getValue().toString())).printToString());
	}

	public static void test() {
		LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();

		print("var a = \"Hello\"; 1+2*3.0 || 2l");
		print("var add = (a,b) -> a+b");
		print("var test:Boolean = 2");
		print("var test2:(Integer,Long)->Boolean");
		print("var addInt:(Integer,Integer)->Integer = (a:Integer,b:Integer) -> a+b");
	}

	public static void main(String[] args) {
		test();
	}
}
