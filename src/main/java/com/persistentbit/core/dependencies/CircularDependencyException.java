package com.persistentbit.core.dependencies;

/**
 * @author Peter Muys
 * @since 26/09/2016
 */
public class CircularDependencyException extends RuntimeException {
    private final Object first;
    private final Object second;

    public CircularDependencyException(Object first, Object second) {
        super("Circular dependency between " + first + " and " + second);
        this.first = first;
        this.second = second;
    }
}
