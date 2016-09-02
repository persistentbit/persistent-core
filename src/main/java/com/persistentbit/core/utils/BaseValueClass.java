package com.persistentbit.core.utils;



/**
 *
 * Classes derived from this class will have a default hashcode/equals method and a toString method.<br>
 * And a method to create a copy of this object with one property changed.<br>
 * This is implemented using {@link ImTools}
 * @see ImTools#copyWith(Object, String, Object)
 * @see ImTools#toStringAll(Object, boolean)
 * @see ImTools#hashCodeAll(Object)
 * @author Peter Muys
 */
public class BaseValueClass {


    @Override
    public int hashCode() {
        ImTools im = ImTools.get(this.getClass());
        return im.hashCodeAll(this);
    }

    @Override
    public boolean equals(Object obj) {
        ImTools im = ImTools.get(this.getClass());
        return im.equalsAll(this,obj);
    }

    @Override
    public String toString() {
        ImTools im = ImTools.get(this.getClass());
        return im.toStringAll(this,true);
    }

    protected <T extends BaseValueClass>  T copyWith(String propertyName, Object value){
        ImTools im = ImTools.get(this.getClass());
        return (T)im.copyWith(this,propertyName,value);
    }
}
