package com.persistentbit.core.glasgolia;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.glasgolia.compiler.CompileGToR;
import com.persistentbit.core.glasgolia.compiler.RStack;
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
	private static CompileGToR compiler = new CompileGToR(new RStack.ReplStack());


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
		print("var a = 100; var b= 23; var c= a+b");
		print("{val a = 100; val b= 23L; val c= a+b; c}");
		print("{val c= a+b;}");
		print("val print = System.out.println;");
		print("print('Hello: ' + c)");
		print("val tekst = 'sum='; ");
		print("val printSum = (a,b) -> { val c = a+b; print(tekst + c)}");
		print("val sum = x:Integer -> y:Integer -> x+y");
		print("val id = u -> u");
		print("id('this is id')");
		print("sum(2)(5);");
		print("printSum(sum(1)(2),sum(4)(6));");
		print("var aVar = 100;");
		print("aVar = 1");
		print("val count = cnt -> { var i = 0; while(i<cnt) { print(i); i=i+1; } }");
		print("count(100)");
		print("val recursive = (x,f)  -> {print(x); if(x != 0) {f(x-1,f);} else 'Done counting ' }");
		print("val rec1 = x -> recursive(x,recursive);");
		print("rec1(1000)");
	}
}
