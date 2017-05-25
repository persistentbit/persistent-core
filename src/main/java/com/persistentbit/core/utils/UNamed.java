package com.persistentbit.core.utils;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utilities to give a Lambda a meaning full toString() method
 *
 * @author Peter Muys
 * @since 13/04/2017
 */
public class UNamed {

	/**
	 * Wraps a {@link Predicate} so that we can have a nice toString() value
	 * @param name the new toString() value
	 * @param pred The predicate to wrap
	 * @param <R> The predicate test type
	 * @return The wrapped predicate
	 */
    public static <R>Predicate<R> namedPredicate(String name, Predicate<R> pred){
        return new Predicate<R>() {
            @Override
            public boolean test(R r) {
                return pred.test(r);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

	/**
	 * Wraps a {@link Function} so that we can have a nice toString() value
	 * @param name The toString value
	 * @param function The function to wrap
	 * @param <T> The Function argument type
	 * @param <R> The function return type
	 * @return The wrapped function
	 * @see #namedPredicate(String, Predicate)
	 */
    public static <T,R> Function<T,R> namedFunction(String name, Function<T,R> function){
        return new Function<T, R>() {
            @Override
            public R apply(T t) {
                return function.apply(t);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }
}
