package com.persistentbit.core.experiments;

import com.persistentbit.core.Result;
import com.persistentbit.core.logging.LogPrinter;
import com.persistentbit.core.logging.Logged;


/**
 * @author petermuys
 * @since 30/12/16
 */
public class LogMonadTest{

	static public int divide(int a, int b) {
		return Logged.function(a, b).log(l -> {
			l.debug("divide ", a, b);
			return a / b;
		});
	}

	static public int add(int a, int b) {
		return Logged.function(a, b).log(l -> {
			l.debug("add", a, b);
			return a + b;
		});
	}

	static public int wrongFunction(int a) {
		return Logged.function(a).log(l -> {
			l.debug("Started WrongFunction", a);
			throw new RuntimeException("WrongFunction FAILED");
		});
	}

	static public int wrongWrongFunction(int a) {
		return Logged.function(a).log(l -> {
			l.debug("wrongWrongFunction", a);
			return wrongFunction(a);
		});
	}

	public static Result<Integer> saveDiv(int a, int b) {
		return Logged.function(a, b).logResult(l -> {
			return b == 0
				? Result.<Integer>failure("Can't divide by 0")
				: Result.success(a / b);
		});

	}

	public static Result<Integer> saveAddDiv(int a, int b, int c) {
		return saveDiv(a + b, c);
	}

	public static Result<Integer> saveDivDiv(int a, int b, int c) {
		return Logged.function(a, b, c).logResult(l -> {
			Result<Integer> firstDiv = saveDiv(a, b);

			return l.add(firstDiv).flatMap(divided -> {
				Result<Integer> secondDiv = saveDiv(divided, c);
				return l.add(secondDiv);
			});
		});
	}

	static public int addDiv(int a, int b, int c) {
		return Logged.function(a, b, c).log(l -> {
			l.debug("addDiv", a, b, c);
			int added = add(a, b);
			l.debug("add result=" + added);
			wrongWrongFunction(a);
			return divide(added, c);
		});
	}


	static public void tryIt(Runnable run) {
		try {
			run.run();
		} catch(Exception e) {
			LogPrinter.consoleInColor().print(e);
		}
	}

	public static void main(String[] args) {
		UserDAO dao = new UserDAO();
		try {
			//tryIt(()-> System.out.println(dao.getUserById(1).orElseThrow()));
			//tryIt(() ->System.out.println(dao.getUserById(2).orElseThrow()));
			//tryIt(() ->System.out.println(dao.getUserById(3).orElseThrow()));
			//tryIt(() ->System.out.println(dao.getUserById(4).orElseThrow()));
			tryIt(() -> System.out.println(saveDivDiv(10, 2, 0).orElseThrow()));
			//tryIt(()->System.out.println(divide(10,2)));
			//tryIt(()->System.out.println(addDiv(10,2,0)));
		} catch(Exception e) {
			LogPrinter.consoleInColor().print(e);
		}

	}
}
