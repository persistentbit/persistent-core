package com.persistentbit.core;

import java.util.Optional;

/**
 * User: petermuys
 * Date: 8/07/16
 * Time: 16:40
 */
public class Tuple2<T1, T2> {
    public final T1 _1;
    public final T2 _2;

    public Tuple2(T1 v1, T2 v2) {
        this._1 = v1;
        this._2 = v2;
    }

    static <T1,T2> Tuple2<T1,T2> of(T1 v1, T2 v2){
        return new Tuple2<>(v1,v2);
    }

    @Override
    public int hashCode() {
        return (_1 == null ? 1 :_1.hashCode()) + (_2 == null ? 1 : _2.hashCode());
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
}
