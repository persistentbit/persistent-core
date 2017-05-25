package com.persistentbit.core.patternmatching;

import com.persistentbit.core.doc.annotations.DSupport;

import java.util.function.Predicate;

/**
 * Utility classes with general Predicates
 *
 * @author petermuys
 * @since 5/04/17
 */
@DSupport
public final class UMatch{
	public static <V> Predicate<V> isNull(){
		return isEqual(null);
	}

	public static <V> Predicate<V> isNotNull() { return UMatch.<V>isNull().negate();}

	public static <V,C> ForClassPredicate<V,C> classIs(Class<C> cls){
		return new ForClassPredicate<>(v -> v != null && v.getClass().equals(cls));
	}


	public static class ForClassPredicate<V,C> implements Predicate<V>{
		private final Predicate<V> master;

		public ForClassPredicate(Predicate<V> master) {
			this.master = master;
		}

		@Override
		public boolean test(V v) {
			return master.test(v);
		}
		public Predicate<V> andForClass(Predicate<C> castedPredicate){
			return and(v -> castedPredicate.test((C)v));
		}
	}



	public static <V,C> ForClassPredicate<V,C> isAssignableTo(Class<? extends C> cls){
		return new ForClassPredicate<>(v -> v != null && cls.isAssignableFrom(v.getClass()) );
	}

	public static <V> Predicate<V> isEqual(V value){
		return Predicate.isEqual(value);
	}

}
