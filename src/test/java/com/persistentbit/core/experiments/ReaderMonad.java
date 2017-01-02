package com.persistentbit.core.experiments;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.tuples.Tuple2;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class ReaderMonad<CTX,A>{
	private final Function<CTX,A> getter;

	private ReaderMonad(Function<CTX, A> getter) {
		this.getter = getter;
	}
	public static <CTX,A> ReaderMonad<CTX,A> of(Function<CTX,A> getter){
		return new ReaderMonad<>(getter);
	}
	public static <CTX,A> ReaderMonad<CTX,A> unit(A value) {
		return of( (CTX ctx) -> value);
	}

	public A apply(CTX context){
		return getter.apply(context);
	}

	public <B> ReaderMonad<CTX,B> map(Function<A,B> f){
		return of((CTX ctx) -> f.apply(apply(ctx)));
	}
	public <B> ReaderMonad<CTX,B> flatMap(Function<A,ReaderMonad<CTX,B>> f){
		return of((CTX ctx) -> f.apply(apply(ctx)).apply(ctx));
	}

	public static <CTX, A> ReaderMonad<CTX, PList<A>> sequence(Iterable<ReaderMonad<CTX, A>> readers) {
		return new ReaderMonad<>(ctx ->
			PList.<A>empty().plusAll(PStream.from(readers).map(r -> r.apply(ctx)))
		);
	}
	public <U> ReaderMonad<CTX, Tuple2<A, U>> zip(ReaderMonad<CTX, U> reader) {
		return this.flatMap(a -> reader.map(b -> Tuple2.of(a, b)));
	}
}
