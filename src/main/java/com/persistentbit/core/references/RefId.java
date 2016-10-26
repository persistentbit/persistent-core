package com.persistentbit.core.references;

import com.persistentbit.core.Immutable;

import java.util.Objects;
import java.util.Optional;

/**
 * An Implementation of a {@link Ref} that only has the ID of the referenced object
 */
@Immutable
public class RefId<R,ID> implements Ref<R,ID>{
    private final ID  id;
    public RefId(ID id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public Optional<R> getValue() {
        return Optional.empty();
    }

    @Override
    public ID getId() {
        return id;
    }

    @Override
    public Ref<R, ID> asIdRef() {
        return this;
    }

    @Override
    public Ref<R, ID> asValueRef(R value) {
        return new RefValue<>(this, value);
    }



    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Ref == false){
            return false;
        }
        if(this == obj){
            return true;
        }
        Ref r = (Ref)obj;
        return getId().equals(r.getId());
    }


    @Override
    public String toString() {
        return "RefId("  + id + ")";
    }
}
