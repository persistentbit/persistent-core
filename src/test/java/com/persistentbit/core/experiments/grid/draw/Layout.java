package com.persistentbit.core.experiments.grid.draw;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class Layout {
    public final Dim dim;
    public final int baseLine;

    public Layout(Dim dim, int baseLine) {
        this.dim = dim;
        this.baseLine = baseLine;
    }
    public Layout withDim(Dim dim){
        return new Layout(dim,baseLine);
    }
    public Layout withBaseLine(int baseLine){
        return new Layout(dim,baseLine);
    }
    public Layout combineHeight(Layout other){
        int maxBl = Math.max(baseLine,other.baseLine);
        int maxDescent = Math.max(dim.height-maxBl,other.dim.height-maxBl);
        return withBaseLine(maxBl).withDim(dim.withHeight(maxBl+maxDescent));
    }
}
