package com.persistentbit.core.experiments;

import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 23/12/16
 */
public class Random<A> extends State<RNG, A>{


	public Random(Function<RNG, StateTuple<A, RNG>> run) {
		super(run);
	}


	public static State<RNG, Integer> intRnd  = new Random<>(RNG::nextInt);
	public static State<RNG, Boolean> boolRnd = intRnd.map(i -> (i % 2) == 0);

	static public class Point extends BaseValueClass{

		private final int x;
		private final int y;
		private final int z;

		public Point(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	public static State<RNG, Point>                rndPointGen     =
		intRnd.flatMap(x -> intRnd.flatMap(y -> intRnd.map(z -> new Point(x, y, z))));
	public static State<RNG, Tuple2<Point, Point>> rndPointPairGen =
		rndPointGen.flatMap(p1 -> rndPointGen.map(p2 -> Tuple2.of(p1, p2)));

	public static void main(String... args) {
		StateTuple<Tuple2<Point, Point>, RNG> s1 = rndPointPairGen.apply(JavaRNG.rng(1));
		StateTuple<Tuple2<Point, Point>, RNG> s2 = rndPointPairGen.apply(s1.state);
		System.out.println(s1.value);
		System.out.println(s2.value);



	}


}
