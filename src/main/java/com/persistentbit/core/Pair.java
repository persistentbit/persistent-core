package com.persistentbit.core;


import java.util.Objects;

/**
 * A Tuple2 with non null values
 * Created by pmu on 8/01/2015.
 */
public class Pair<L,R> extends Tuple2<L,R> {

    public Pair(L left, R right){
        super(
                Objects.requireNonNull(left),
                Objects.requireNonNull(right));
    }



    @Override
    public String toString() {
        return "Pair[" +
                getLeft() +
                ", " + getRight() +
                ']';
    }

    public L getLeft() {
        return _1;
    }

    public R getRight() {
        return _2;
    }


}