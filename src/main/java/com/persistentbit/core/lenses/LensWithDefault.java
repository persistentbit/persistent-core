package com.persistentbit.core.lenses;

/**
 * A Lens where the Getter returns a default value when the originial value is null
 * @author Peter Muys
 * @since 20/06/2016
 */
public class LensWithDefault<P,C> implements Lens<P,C> {
    private final Lens<P,C> lens;
    private final C             defaultChildValue;

    public LensWithDefault(Lens<P, C> lens, C defaultChildValue) {
        this.lens = lens;
        this.defaultChildValue = defaultChildValue;
    }

    public C    get(P parent){
        C child = lens.get(parent);
        if(child == null){
            return defaultChildValue;
        }
        return child;
    }

    public P set(P parent, C client){
        return lens.set(parent,client == null ? defaultChildValue : client);
    }
}
