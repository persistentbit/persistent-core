package com.persistentbit.core.references;

import java.util.Objects;
import java.util.Optional;

/**
 * An implementation of a {@link Ref} that has the ID and the referenced object.<br>
 */
public class RefValue<R,ID> implements Ref<R,ID>{

    private final RefId<R,ID> refId;
    private final R value;

    public RefValue(RefId<R,ID> refId, R value) {
        this.refId = Objects.requireNonNull(refId);
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public Optional<R> getValue() {
        return Optional.of(value);
    }

    @Override
    public ID getId() {
        return refId.getId();
    }

    @Override
    public Ref<R, ID> asIdRef() {
        return refId;
    }

    @Override
    public Ref<R, ID> asValueRef(R value) {
        return new RefValue<R, ID>(refId,value);
    }


    @Override
    public String toString() {
        return "RefValue(" + value + ")";
    }
}
