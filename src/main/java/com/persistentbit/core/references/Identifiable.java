package com.persistentbit.core.references;

/**
 * An Identifiable Object has a unique Id to identify the object<br>
 * @param <ID> The type of the ID
 */
public interface Identifiable<ID> {
    /**
     * Get the unique ID for this object
     * @return The ID
     */
    ID getId();
}
