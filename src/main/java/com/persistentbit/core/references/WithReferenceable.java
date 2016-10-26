package com.persistentbit.core.references;

/**
 * Mixin class for Objects that have an id to automatically generate {@link RefId}'s or {@link RefValue}'s
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface WithReferenceable<T,ID> extends Identifiable<ID>,Referenceable<T,ID> {
    @Override
    default RefId<T, ID> getIdRef() {
        return new RefId<>(getId());
    }

    @SuppressWarnings("unchecked")
    @Override
    default RefValue<T, ID> getValueRef() {
        return new RefValue<>(getIdRef(),(T)this);
    }
}
