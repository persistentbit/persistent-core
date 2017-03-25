package com.persistentbit.core.experiments.grid.draw;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/03/2017
 */
public class DPoint {
    public final int x;
    public final int y;

    public DPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public DPoint(){
        this(0,0);
    }

    static public DPoint of(int x, int y){
    	return new DPoint(x,y);
	}

    public DPoint withX(int x){
        return new DPoint(x,y);
    }
    public DPoint withY(int y){
        return new DPoint(x,y);
    }
    public DPoint addX(int dx){
        return withX(x+dx);
    }
    public DPoint addY(int dy){
        return withY(y+dy);
    }
    public DPoint addXY(int x, int y){
        return addX(x).addY(y);
    }
    public DPoint add(DPoint other){
        return addXY(other.x,other.y);
    }
    public DPoint add(Dim other) {
        return addXY(other.width,other.height);
    }
}
