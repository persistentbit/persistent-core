package com.persistentbit.core.experiments;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/12/16
 */
public class State<S, A> implements Function<S, StateTuple<A, S>>{

	private final Function<S, StateTuple<A, S>> run;

	public State(Function<S, StateTuple<A, S>> run) {
		this.run = run;
	}

	@Override
	public StateTuple<A, S> apply(S s) {
		return run.apply(s);
	}

	public static <S, A> State<S, A> unit(A a) {
		return new State<>(s -> new StateTuple<A, S>(a, s));
	}

	public <B> State<S, B> flatMap(Function<A, State<S, B>> f) {
		return new State<>(s -> {
			StateTuple<A, S> aRes = run.apply(s);
			return f.apply(aRes.value).run.apply(aRes.state);
		});
	}

	public <B> State<S, B> map(Function<A, B> f) {
		return flatMap(a -> State.unit(f.apply(a)));
	}

	public <B, C> State<S, C> map2(State<S, B> sb, Function<A, Function<B, C>> f) {
		return flatMap(a -> sb.map(b -> f.apply(a).apply(b)));
	}
/*
	public static <S> State<S,S> get() {
		return
	}

	public static <S> State<S,Nothing> modify(Function<S, S> f){
		return State.<S>g
	} */

}
