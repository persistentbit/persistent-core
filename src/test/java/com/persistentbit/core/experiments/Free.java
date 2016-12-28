package com.persistentbit.core.experiments;

/**
 * TODOC
 *
 * @author petermuys
 * @since 27/12/16
 */
public interface Free<F extends Functor<?>, A>{

	class Return<F extends Functor<?>, A> implements Free<Functor<?>, A>{

		public final A a;

		public Return(A a) {
			this.a = a;
		}
	}

	class Suspend<F extends Functor<?>, A> implements Free<Functor<?>, A>{

		private final Functor<Free<Functor<?>, A>> s;

		public Suspend(
			Functor<Free<Functor<?>, A>> s
		) {
			this.s = s;
		}
	}
}
