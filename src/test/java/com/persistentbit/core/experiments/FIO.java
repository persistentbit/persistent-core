package com.persistentbit.core.experiments;

import com.persistentbit.core.Nothing;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/12/16
 */
@FunctionalInterface
public interface FIO<A>{

	A run();

	default <B> FIO<B> map(Function<A, B> f) {
		return () -> f.apply(this.run());
	}

	default <B> FIO<B> flatMap(Function<A, FIO<B>> f) {
		return () -> f.apply(this.run()).run();
	}

	FIO<Nothing> empty = () -> Nothing.inst;

	static <A> FIO<A> unit(A a) {
		return () -> a;
	}

	static <A, B, C> FIO<C> map2(FIO<A> ioa, FIO<B> iob, Function<A, Function<B, C>> f) {
		return ioa.flatMap(a -> iob.map(b -> f.apply(a).apply(b)));
	}

	/*static <A> FIO<PStream<A>> repeat(int count, FIO<A> io) {
		PStream<FIO<A>>                               stream = PStream.repeatValue(io).limit(count);
		Function<A, Function<PStream<A>, PStream<A>>> f      = a -> la -> la.plus(a);
		FIO<PStream<A>>                               unit   = FIO.unit(PStream.val());
		return stream.foldRight(unit, f);
	}*/
}
