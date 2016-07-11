package com.persistentbit.core.lenses;

import java.util.function.Function;

/**
 * A Lens is an object that can be used to update immutable data.<br>
 * See {@link LensImpl} for the main implementation
 * @author Peter Muys
 * @since 20/06/2016
 *
 */
public interface Lens<P,C> {
    /**
     * Create a new P parent object, containing the new client object
     * @param parent The container
     * @param client The value to set
     * @return A new P object with the provided C client.
     */
    P set(P parent, C client);

    /**
     * Ask the C client property of a P parent object
     * @param parent The Container
     * @return The Client property
     */
    C get(P parent);

    /**
     * Set the client property by transforming the existing property
     * @param parent    The container object
     * @param updater   The client property provider
     * @return A new P object the the new C property
     */
    default P update(P parent, Function<C, C> updater){
        C org = get(parent);
        C newValue = updater.apply(org);
        return set(parent,newValue);
    }

    /**
     * Is the client C property fot the given container P !=null
     * @param parent The container object
     * @return true if the client property is not null
     */
    default boolean exists(P parent) {
        return get(parent) != null;
    }

    /**
     * Join 2 lenses so we get: P- C- CC
     * @param subLens The sublens to add
     * @param <CC> Type of the C client property property
     * @return A new Lens from P - CC
     */
    default <CC> Lens<P,CC> andThen(Lens<C, CC> subLens){
        return new ThenLens<>(this,subLens);
    }
    default Lens<P,C> withDefaultChildValue(C defaultChildValue){
        return new LensWithDefault<P, C>(this,defaultChildValue);
    }
}
