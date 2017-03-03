package com.persistentbit.core.glasgolia;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.glasgolia.compiler.CompileGToR;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.glasgolia.gexpr.GExpr;
import com.persistentbit.core.glasgolia.gexpr.GExprParser;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.source.Source;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class RCompileTest{

	private static GExprParser parser = new GExprParser();
	private static CompileGToR compiler = new CompileGToR();


	private static void print(String code) {
		System.out.println(code);
		System.out.println();
		GExprParser        parser = new GExprParser();
		ParseResult<GExpr> result = parser.parseExprList().parse(Source.asSource("test", code));
		GExpr              expr   = result.getValue();
		System.out.println("Parsed:" + expr);
		System.out.println();
		RExpr r = compiler.compile(expr);
		System.out.println("Compiled: " + r);
		System.out.println();
		System.out.println("Result:" + r.get());
		System.out.println("-----");

	}

	public static void main(String[] args) {
		LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();
		print("(1+2)*3L");
		print("1;2");
		print("import 'com.persistentbit.core.glasgolia'; GExprTest.test");
		print("import 'com.persistentbit.core.glasgolia'; 'tostring=' + GExprTest('Hello').toString():String");
		print("import 'java.lang'; System.out.println('This is from system out:' + 1234);System.out.println()");

	}
}
