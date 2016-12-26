package com.persistentbit.core.experiments;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.function.F;

import java.math.BigInteger;

/**
 * TODOC
 *
 * @author petermuys
 * @since 26/12/16
 */
public class Fib{

	private static TailCall<BigInteger> fib_(BigInteger acc1, BigInteger acc2, BigInteger x) {
		if(x.equals(BigInteger.ZERO)) {
			return TailCall.ret(BigInteger.ZERO);
		}
		else if(x.equals(BigInteger.ONE)) {
			return TailCall.ret(acc1.add(acc2));
		}
		return TailCall.suspend(() -> fib_(acc2, acc1.add(acc2), x.subtract(BigInteger.ONE)));
	}

	public static BigInteger fib(int number) {
		return fib_(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(number)).eval();
	}

	public static <T, U> U foldLeft(PStream<T> list, U init, F<U, F<T, U>> f) {
		return foldLeft_(list, init, f).eval();
	}

	public static <T, U> TailCall<U> foldLeft_(PStream<T> list, U init, F<U, F<T, U>> f) {
		return list.isEmpty()
			? TailCall.ret(init)
			: TailCall.suspend(() -> foldLeft_(list.tail(), f.apply(init).apply(list.head()), f));
	}

	public static void main(String[] args) {
		//PStream.sequence(0).limit(20000).map(Fib::fib).forEach(System.out::println);

	}
}
