package com.persistentbit.core.references;


import java.util.Optional;

/**
 * A Ref represents the Id of an object, and could have the actual object.<br>
 * A Ref always has a non null Id and kan have a none null Value.<br>
 *
 * @param <T> The Type of the Object
 * @param <ID> The Type of the ID for the Object
 * @see RefId
 * @see RefValue
 */
public interface Ref<T,ID> {
    /**
     *
     * @return The Optional Object that this Ref references or empty if we only have an ID.
     */
    Optional<T> getValue();

    /**
     *
     * @return The ID of the Object we reference
     */
    ID getId();

    /**
     * Get this Ref as an ID only reference. Any known instance of the Referenced value is removed
     * @return The new created Ref with an ID
     */
    Ref<T,ID>   asIdRef();

    /**
     * Create a new Ref with this ID and the provided value
     * @param value The new referenced value
     * @return A new Reference value object
     */
    Ref<T,ID>   asValueRef(T value);




}