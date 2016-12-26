package com.persistentbit.core.experiments;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.tuples.Tuple2;
import org.junit.Test;

/**
 * TODOC
 *
 * @author petermuys
 * @since 23/12/16
 */
public class RNGTest{

	@Test
	public void testInteger() throws Exception {
		RNG                  rng = JavaRNG.rng(0);
		Tuple2<Integer, RNG> t1  = RNGenerator.integer(rng);
		assert -1155484576 == t1._1;
		Tuple2<Integer, RNG> t2 = RNGenerator.integer(t1._2);
		assert -723955400 == t2._1;
		Tuple2<Integer, RNG> t3 = RNGenerator.integer(t2._2);
		assert 1033096058 == t3._1;
	}

	@Test
	public void testIntegerList() {
		RNG                         rng       = JavaRNG.rng(0);
		Tuple2<PList<Integer>, RNG> listTuple = RNGenerator.integers(rng, 3);
		listTuple._1.forEach(System.out::println);
	}

	@Test
	public void testRandom() {
	}
}
