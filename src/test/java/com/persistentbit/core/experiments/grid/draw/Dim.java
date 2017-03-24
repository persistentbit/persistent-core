package com.persistentbit.core.experiments.grid.draw;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class Dim {
    public final int width;
    public final int height;

    public Dim(int width, int height) {
        this.width = width;
        this.height = height;
    }
    public Dim(){
        this(0,0);
    }
    public Dim withWidth(int width){
        return new Dim(width, height);
    }
    public Dim withHeight(int height){
        return new Dim(width,height);
    }
    public Dim addWidth(int width){
        return withWidth(this.width+width);
    }
    public Dim addHeight(int height){
        return withHeight(this.height+height);
    }
}
