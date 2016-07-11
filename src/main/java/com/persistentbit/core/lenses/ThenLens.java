package com.persistentbit.core.lenses;

/**
 * A Lens that is the combination of 2 other lenses.<br>
 * So we get from P to C to CC.<br>
 * @author Peter Muys
 * @since 20/06/2016
 */
public class ThenLens<P,C,CC> implements Lens<P,CC> {
    private final Lens<P,C> first;
    private final Lens<C,CC> next;

    public ThenLens(Lens<P, C> first, Lens<C, CC> next) {
        this.first = first;
        this.next = next;
    }

    @Override
    public P set(P parent, CC client) {
        return first.set(parent,next.set(first.get(parent),client));
    }

    @Override
    public CC get(P parent) {
        return next.get(first.get(parent));
    }
    @Override
    public boolean exists(P parent) {
        if(first.exists(parent) == false){
            return false;
        }
        return next.exists(first.get(parent));
    }
}
