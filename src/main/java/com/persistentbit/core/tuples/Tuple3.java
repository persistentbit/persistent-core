package com.persistentbit.core.tuples;

import com.persistentbit.core.Immutable;
import com.persistentbit.core.Nullable;
import com.persistentbit.core.function.Function3;
import com.persistentbit.core.properties.FieldNames;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

/**
 * Contains 3 nullable values with possible different types.
 * @param <T1> Type of value 1
 * @param <T2> Type of value 2
 * @param <T3> Type of value 3
 * @author Peter Muys
 * @since 30/09/2016
 * @see Tuple2
 * @see Tuple4
 * @see Tuple5
 * @see Tuple6
 * @see Tuple7
 */
@Immutable
public class Tuple3<T1, T2, T3> implements Comparable<Tuple3<T1, T2, T3>>, Serializable {

    @Nullable
    public final T1 _1;
    @Nullable
    public final T2 _2;
    @Nullable
    public final T3 _3;

    /**
     * Create a new Tuple3 with 3 values
     *
     * @param v1 First value
     * @param v2 Second value
     * @param v3 Third value
     */
    @FieldNames(names = {"_1", "_2", "_3"})
    public Tuple3(@Nullable T1 v1, @Nullable T2 v2, @Nullable T3 v3) {
        this._1 = v1;
        this._2 = v2;
        this._3 = v3;
    }

    /**
     * Create a new Tuple3 from the given values.
     *
     * @param v1   _1 value
     * @param v2   _2 value
     * @param v3   _3 value
     * @param <T1> Type of _1
     * @param <T2> Type of _2
     * @param <T3> Type of _3
     * @return a new Tuple3
     */
    public static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 v1, T2 v2, T3 v3) {
        return new Tuple3<>(v1, v2, v3);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>) o;

        if (_1 != null ? !_1.equals(tuple3._1) : tuple3._1 != null) return false;
        if (_2 != null ? !_2.equals(tuple3._2) : tuple3._2 != null) return false;
        return _3 != null ? _3.equals(tuple3._3) : tuple3._3 == null;

    }

    @Override
    public int hashCode() {
        int result = _1 != null ? _1.hashCode() : 0;
        result = 31 * result + (_2 != null ? _2.hashCode() : 0);
        result = 31 * result + (_3 != null ? _3.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "(" + _1 + "," + _2 + ", " + _3 + ")";
    }

    public Optional<T1> get1() {
        return Optional.ofNullable(_1);
    }

    public Optional<T2> get2() {
        return Optional.ofNullable(_2);
    }

    public Optional<T3> get3() {
        return Optional.ofNullable(_3);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(Tuple3<T1, T2, T3> o) {
        int r = this.dropLast().compareTo(o.dropLast());
        if (r != 0) {
            return r;
        } else {
            if (this._3 == null) {
                return o._3 == null ? 0 : -1;
            }
            //noinspection ConstantConditions
            return ((Comparable) this._3).compareTo(o._3);
        }
    }

    public Tuple2<T1, T2> dropLast() {
        return Tuple2.of(_1, _2);
    }

    public Tuple3<T1, T2, T3> with_1(T1 value) {
        return new Tuple3<>(value, this._2, this._3);
    }

    public Tuple3<T1, T2, T3> with_2(T2 value) {
        return new Tuple3<>(this._1, value, this._3);
    }

    public Tuple3<T1, T2, T3> with_3(T3 value) {
        return new Tuple3<>(this._1, this._2, value);
    }

    public <R> R map(Function3<T1, T2, T3, R> map) {
        return map.apply(_1, _2, _3);
    }

    public <R1> Tuple3<R1,T2,T3> map1(Function<T1,R1> map){
        return Tuple3.of(map.apply(_1),_2,_3);
    }
    public <R2> Tuple3<T1,R2,T3> map2(Function<T2,R2> map){
        return Tuple3.of(_1,map.apply(_2),_3);
    }
    public <R3> Tuple3<T1,T2,R3> map3(Function<T3,R3> map){
        return Tuple3.of(_1,_2,map.apply(_3));
    }
}
