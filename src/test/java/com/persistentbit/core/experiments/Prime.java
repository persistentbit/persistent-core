package com.persistentbit.core.experiments;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.tuples.Tuple2;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 27/12/16
 */
public class Prime{

	static public boolean isFactor(int potential, int number) {
		return number % potential == 0;
	}

	static public PStream<Integer> primes() {
		return PStream.val(1, 2).plusAll(PStream.sequence(3, Prime::nextPrime));
	}

	static public Integer nextPrime(int lastPrime) {
		lastPrime++;
		while(!isPrime(lastPrime)) lastPrime++;
		return lastPrime;
	}

	static public Function<Integer, Integer> cached_nextPrime = Memoizer.of(Prime::nextPrime);

	static public boolean isPrime(int n) {
		int sf = sumFactors(n);
		return n == 2 || n + 1 == sf;
	}

	static public int sumFactors(int n) {
		return getFactors(n).fold(0, a -> b -> a + b);
	}

	static public PStream<Integer> getFactors(int n) {
		return PStream.sequence(1).limit(n).filter(i -> isFactor(i, n));
	}

	static public Integer squareRoot(int n) {
		return (int) Math.sqrt(n);
	}

	public static void main(String[] args) {
		PStream.sequence(1).limit(20).map(i -> Tuple2.of(i, getFactors(i).plist())).forEach(System.out::println);

		primes().until(p -> p <= 2000).filter(p -> p > 1000).plist().forEach(System.out::println);
	}
}
