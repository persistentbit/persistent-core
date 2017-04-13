package com.persistentbit.core.utils;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 13/04/2017
 */
public class UNamed {
    static public <R>Predicate<R> named(String name, Predicate<R> pred){
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
    static public <T,R> Function<T,R> named(String name, Function<T,R> function){
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
