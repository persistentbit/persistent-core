package com.persistentbit.core.experiments;

import com.persistbit.core.CoreTest;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.IO;

import java.io.File;
import java.nio.charset.Charset;


/**
 * @author petermuys
 * @since 30/12/16
 */
public class LogMonadTest{

	static public int divide(int a, int b) {
		return Log.function(a, b).code(l -> {
			l.info("divide ", a, b);
			return a / b;
		});
	}

	static public int add(int a, int b) {
		return Log.function(a, b).code(l -> {
			l.info("add", a, b);
			return a + b;
		});
	}

	static public int wrongFunction(int a) {
		return Log.function(a).code(l -> {
			l.info("Started WrongFunction", a);
			throw new RuntimeException("WrongFunction FAILED");
		});
	}

	static public int wrongWrongFunction(int a) {
		return Log.function(a).code(l -> {
			l.info("wrongWrongFunction", a);
			return wrongFunction(a);
		});
	}

	public static Result<Integer> saveDiv(int a, int b) {
		return
			b == 0
				? Result.<Integer>failure("Can't divide by 0").logFunction(a,b)
				: Result.success(a / b).logFunction(a,b)
		;

	}

	public static Result<Integer> saveAddDiv(int a, int b, int c) {
		return saveDiv(a + b, c).logFunction(a,b,c);
	}

	public static Result<Integer> saveDivDiv(int a, int b, int c) {
		return
			saveDiv(a,b)
					.flatMap(divided ->	saveDiv(divided, c)
					.logFunction(a,b,c)
		);
	}

	static public int addDiv(int a, int b, int c) {
		return Log.function(a, b, c).code(l -> {
			l.info("addDiv", a, b, c);
			int added = add(a, b);
			l.info("add result=" + added);
			wrongWrongFunction(a);
			return divide(added, c);
		});
	}


	static public void tryIt(Runnable run) {
		try {
			run.run();
		} catch(Exception e) {
			CoreTest.testLogPrint.print(e);
		}
	}

	public static void main(String[] args) {
		UserDAO  dao = new UserDAO();
		LogPrint lp  = CoreTest.testLogPrint;
		lp.print(dao.getUserById(1).getLog());
		lp.print(IO.readTextFile(new File("UnknownFile"), Charset.defaultCharset()).getLog());
		tryIt(() -> {

			IO.readTextFile(new File("UnknownFile"),Charset.defaultCharset()).orElseThrow();
		});

		try {
			//tryIt(()-> System.out.println(dao.getUserById(1).orElseThrow()));
			//tryIt(() ->System.out.println(dao.getUserById(2).orElseThrow()));
			//tryIt(() ->System.out.println(dao.getUserById(3).orElseThrow()));
			//tryIt(() ->System.out.println(dao.getUserById(4).orElseThrow()));
			tryIt(() -> System.out.println(saveDivDiv(10, 2, 0).orElseThrow()));
			//tryIt(()->System.out.println(divide(10,2)));
			//tryIt(()->System.out.println(addDiv(10,2,0)));
		} catch(Exception e) {
			CoreTest.testLogPrint.print(e);
		}

	}
}
