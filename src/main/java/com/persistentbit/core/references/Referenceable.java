package com.persistentbit.core.references;

/**
 * Represents an object that can be referenced with a {@link Ref}
 */
public interface Referenceable<T, ID>{

  /**
   * @return an ID only reference
   */
  RefId<T, ID> getIdRef();

  /**
   * @return A reference with this object and the ID
   */
  RefValue<T, ID> getValueRef();
}
