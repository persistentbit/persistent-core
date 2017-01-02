package com.persistentbit.core.experiments;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 30/12/16
 */
public class WriterMonad<W,A>{
	private final W log;
	private final A value;
	private final W zero;
	private final Function<W,Function<W,W>> append;


	public WriterMonad(W log, A value, W zero, Function<W, Function<W, W>> append) {
		this.log = log;
		this.value = value;
		this.zero = zero;
		this.append = append;
	}

	public static <W,A> WriterMonad<W,A> of(W log, W zero, Function<W,Function<W,W>> append, A value){
		return new WriterMonad<>(log,value,zero,append);
	}
	public <B> WriterMonad<W,B> map(Function<A,B> f){
		return of(log,zero,append,f.apply(value));
	}

	public <B> WriterMonad<W,B> flatMap(Function<A,WriterMonad<W,B>> f){
		WriterMonad<W,B> temp = f.apply(value);
		return of(append.apply(log).apply(temp.log),zero,append,temp.value);
	}
}
