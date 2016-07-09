package com.persistentbit.core.utils;

import com.persistentbit.core.lenses.Lens;

/**
 * @author Peter Muys
 * @since 3/03/2016
 */
public class ImLens<P,C> implements Lens<P,C> {
    private String propertyName;
    public ImLens(String propertyName){
        this.propertyName = propertyName;
    }

    @Override
    public P set(P parent, C client) {
        ImTools<P> im = (ImTools<P>)ImTools.get(parent.getClass());
        return im.copyWith(parent,propertyName,client);
    }

    @Override
    public C get(P parent) {
        ImTools<P> im = (ImTools<P>)ImTools.get(parent.getClass());
        return (C)im.get(parent,propertyName);
    }
}
