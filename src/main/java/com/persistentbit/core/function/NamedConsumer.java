package com.persistentbit.core.function;

/**
 * A consumer for a value T and it's name<br>
 * @param <T> The type of the named value
 * @author Peter Muys
 * @since 15/07/2016
 */
@FunctionalInterface
public interface NamedConsumer<T>{

    void accept(String name, T value);
}
