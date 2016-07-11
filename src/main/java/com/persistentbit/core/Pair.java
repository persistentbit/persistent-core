package com.persistentbit.core;


import com.persistentbit.core.immutable.Immutable;

import java.util.Objects;

/**
 * A Tuple2 with non null values
 * Created by pmu on 8/01/2015.
 */
@Immutable
public class Pair<L,R> extends Tuple2<L,R> {

    public Pair(L _1, R _2){
        super(
                Objects.requireNonNull(_1),
                Objects.requireNonNull(_2));
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