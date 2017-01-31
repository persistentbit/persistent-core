package com.persistentbit.core.tuples;

import com.persistentbit.core.Immutable;
import com.persistentbit.core.Nullable;
import com.persistentbit.core.function.Function2;
import com.persistentbit.core.properties.FieldNames;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

/**
 * A Tuple2 contains 2 nullable values with possible different types.
 *
 * @author Peter Muys
 */
@Immutable
public class Tuple2<T1, T2> implements Comparable<Tuple2<T1, T2>>, Serializable {

    @Nullable
    public final T1 _1;
    @Nullable
    public final T2 _2;

    /**
     * Create a new Tuple2 with 2 values
     *
     * @param v1 First value
     * @param v2 Second value
     */
    @FieldNames(names = {"_1", "_2"})
    public Tuple2(@Nullable T1 v1, @Nullable T2 v2) {
        this._1 = v1;
        this._2 = v2;
    }

    /**
     * Create a new Tuple2 from the given values.
     *
     * @param v1   _1 value
     * @param v2   _2 value
     * @param <T1> Type of _1
     * @param <T2> Type of _2
     * @return a new Tuple2
     */
    public static <T1, T2> Tuple2<T1, T2> of(T1 v1, T2 v2) {
        return new Tuple2<>(v1, v2);
    }

    public <T3> Tuple3<T1, T2, T3> add(T3 v3) {
        return Tuple3.of(_1, _2, v3);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;

        if (_1 != null ? !_1.equals(tuple2._1) : tuple2._1 != null) return false;
        return _2 != null ? _2.equals(tuple2._2) : tuple2._2 == null;

    }

    @Override
    public int hashCode() {
        int result = _1 != null ? _1.hashCode() : 0;
        result = 31 * result + (_2 != null ? _2.hashCode() : 0);
        return result;
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
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public int compareTo(Tuple2<T1, T2> o) {
        Comparable<T1> c1 = (Comparable<T1>) _1;
        if (c1 == null) {
            return o._1 == null ? 0 : -1;
        }
        int r = c1.compareTo(o._1);
        if (r != 0) {
            return r;
        }
        Comparable<T2> c2 = (Comparable<T2>) _2;
        if (c2 == null) {
            return o._2 == null ? 0 : -1;
        }
        return c2.compareTo(o._2);
    }


    public Tuple2<T1, T2> with_1(T1 value) {
        return new Tuple2<>(value, this._2);
    }

    public Tuple2<T1, T2> with_2(T2 value) {
        return new Tuple2<>(this._1, value);
    }

    public <R> R map(Function2<T1, T2, R> map) {
        return map.apply(_1, _2);
    }
    public <R1> Tuple2<R1,T2> map1(Function<T1,R1> map){
        return Tuple2.of(map.apply(_1),_2);
    }
    public <R2> Tuple2<T1,R2> map2(Function<T2,R2> map){
        return Tuple2.of(_1,map.apply(_2));
    }

}
