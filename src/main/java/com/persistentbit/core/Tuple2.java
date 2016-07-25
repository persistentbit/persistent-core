package com.persistentbit.core;

import com.persistentbit.core.codegen.GenNoGetter;
import com.persistentbit.core.properties.FieldNames;


import java.io.Serializable;
import java.util.Optional;

/**
 * A Tuple2 contains 2 nullable values
 *
 * User: petermuys
 * Date: 8/07/16
 * Time: 16:40
 */
@Immutable
public class Tuple2<T1, T2> implements Comparable<Tuple2<T1,T2>>,Serializable{
    @GenNoGetter @Nullable  public final T1 _1;
    @GenNoGetter @Nullable  public final T2 _2;

    /**
     * Create a new Tuple2 with 2 values
     * @param v1 First value
     * @param v2 Second value
     */
    @FieldNames(names = {"_1","_2"})
    public Tuple2(T1 v1, T2 v2) {
        this._1 = v1;
        this._2 = v2;
    }

    /**
     * Create a new Tuple2 from the given values.
     * @param v1 _1 value
     * @param v2 _2 value
     * @param <T1> Type of _1
     * @param <T2> Type of _2
     * @return a new Tuple2
     */
    static public <T1,T2> Tuple2<T1,T2> of(T1 v1, T2 v2){
        return new Tuple2<>(v1,v2);
    }

    @Override
    public int hashCode() {
        return (_1 == null ? 1 :_1.hashCode()) + (_2 == null ? 1 : _2.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) { return true; }
        if(obj instanceof Tuple2 == false){
            return false;
        }
        Tuple2 other = (Tuple2)obj;
        if(this._1 == null){
            return other._1 == null;
        }
        if(this._2 == null){
            return other._2 == null;
        }
        return this._1.equals(other._1) && this._2.equals(other._2);
    }

    @Override
    public String toString() {
        return "(" + _1 + "," + _2 + ")";
    }

    public Optional<T1> get1() {
        return Optional.ofNullable(_1);
    }

    public Optional<T2> get2() {
        return Optional.ofNullable(_2);
    }

    @Override
    public int compareTo(Tuple2<T1, T2> o) {
        Comparable<T1> c1 = (Comparable<T1>)this;
        int r =c1.compareTo(o._1);
        if(r != 0){
            return r;
        }
        Comparable<T2> c2 = (Comparable<T2>)this;
        return c2.compareTo(o._2);
    }





	//Generated by com.persistentbit.core.codegen.ImmutableCodeBuilder

	public Tuple2<T1,T2>	 with_1(T1 value){
		return new Tuple2<>(value, this._2);
	} 

	public Tuple2<T1,T2>	 with_2(T2 value){
		return new Tuple2<>(this._1, value);
	} 

}