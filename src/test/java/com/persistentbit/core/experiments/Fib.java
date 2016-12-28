package com.persistentbit.core.experiments;

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



	public static void main(String[] args) {
		//PStream.sequence(0).limit(20000).map(Fib::fib).forEach(System.out::println);

	}
}
