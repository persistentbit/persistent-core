package com.persistentbit.core.experiments.grid.draw;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class DRect {
    public final DPoint p1;
    public final Dim    dim;

    public DRect(DPoint origin, Dim dim) {
        this.p1 = origin;
        this.dim = dim;
    }
    public DPoint getP2(){
        return p1.add(dim);
    }
}
