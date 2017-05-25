package com.persistentbit.core.tuples;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.function.Function7;
import com.persistentbit.core.properties.FieldNames;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

/**
 * Contains 6 nullable values with possible different types.
 * @param <T1> Type of value 1
 * @param <T2> Type of value 2
 * @param <T3> Type of value 3
 * @param <T4> Type of value 4
 * @param <T5> Type of value 5
 * @param <T6> Type of value 6
 * @param <T7> Type of value 6
 * @author Peter Muys
 * @since 30/09/2016
 * @see Tuple2
 * @see Tuple3
 * @see Tuple4
 * @see Tuple5
 * @see Tuple6
 */
public class Tuple7<T1, T2, T3, T4, T5, T6, T7> implements Comparable<Tuple7<T1, T2, T3, T4, T5, T6, T7>>, Serializable{

	@Nullable
	public final T1 _1;
	@Nullable
	public final T2 _2;
	@Nullable
	public final T3 _3;
	@Nullable
	public final T4 _4;
	@Nullable
	public final T5 _5;
	@Nullable
	public final T6 _6;
	@Nullable
	public final T7 _7;

	@FieldNames(
		names = {"_1", "_2", "_3", "_4", "_5", "_6", "_7"}
	)
	public Tuple7(@Nullable T1 v1, @Nullable T2 v2, @Nullable T3 v3, @Nullable T4 v4, @Nullable T5 v5, @Nullable T6 v6,
	              @Nullable T7 v7
	) {
		this._1 = v1;
		this._2 = v2;
		this._3 = v3;
		this._4 = v4;
		this._5 = v5;
		this._6 = v6;
		this._7 = v7;
	}

	public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> of(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5,
	                                                                                 T6 v6, T7 v7
	) {
		return new Tuple7<>(v1, v2, v3, v4, v5, v6, v7);
	}


	public String toString() {
		return "(" + this._1 + "," + this._2 + ", " +
			       this._3 + "," + this._4 + "," + this._5 + "," + this._6 + "," + this._7 + ")";
	}

	public Optional<T1> get1() {
		return Optional.ofNullable(this._1);
	}

	public Optional<T2> get2() {
		return Optional.ofNullable(this._2);
	}

	public Optional<T3> get3() {
		return Optional.ofNullable(this._3);
	}

	public Optional<T4> get4() {
		return Optional.ofNullable(this._4);
	}

	public Optional<T5> get5() {
		return Optional.ofNullable(this._5);
	}

	public Optional<T6> get6() {
		return Optional.ofNullable(this._6);
	}

	public Optional<T7> get7() {
		return Optional.ofNullable(this._7);
	}

	@Override
	@SuppressWarnings({"unchecked", "ConstantConditions"})
	public int compareTo(Tuple7<T1, T2, T3, T4, T5, T6, T7> o) {
		int r = this.dropLast().compareTo(o.dropLast());
		if(r != 0) {
			return r;
		}
		else {
			if(this._7 == null) {
				return o._7 == null ? 0 : -1;
			}
			return ((Comparable) this._7).compareTo(o._7);
		}
	}

    /*<T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> add(T8 v8){
		return Tuple7.of(_1,_2,_3,_4,_5,_6,_7,v8);
    }*/

	public Tuple6<T1, T2, T3, T4, T5, T6> dropLast() {
		return Tuple6.of(_1, _2, _3, _4, _5, _6);
	}

	public Tuple7<T1, T2, T3, T4, T5, T6, T7> with_1(T1 value) {
		return new Tuple7<>(value, this._2, this._3, this._4, this._5, this._6, this._7);
	}

	public Tuple7<T1, T2, T3, T4, T5, T6, T7> with_2(T2 value) {
		return new Tuple7<>(this._1, value, this._3, this._4, this._5, this._6, this._7);
	}

	public Tuple7<T1, T2, T3, T4, T5, T6, T7> with_3(T3 value) {
		return new Tuple7<>(this._1, this._2, value, this._4, this._5, this._6, this._7);
	}

	public Tuple7<T1, T2, T3, T4, T5, T6, T7> with_4(T4 value) {
		return new Tuple7<>(this._1, this._2, this._3, value, this._5, this._6, this._7);
	}

	public Tuple7<T1, T2, T3, T4, T5, T6, T7> with_5(T5 value) {
		return new Tuple7<>(this._1, this._2, this._3, this._4, value, this._6, this._7);
	}

	public Tuple7<T1, T2, T3, T4, T5, T6, T7> with_6(T6 value) {
		return new Tuple7<>(this._1, this._2, this._3, this._4, this._5, value, this._7);
	}

	public Tuple7<T1, T2, T3, T4, T5, T6, T7> with_7(T7 value) {
		return new Tuple7<>(this._1, this._2, this._3, this._4, this._5, this._6, value);
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		Tuple7<?, ?, ?, ?, ?, ?, ?> tuple7 = (Tuple7<?, ?, ?, ?, ?, ?, ?>) o;

		if(_1 != null ? !_1.equals(tuple7._1) : tuple7._1 != null) return false;
		if(_2 != null ? !_2.equals(tuple7._2) : tuple7._2 != null) return false;
		if(_3 != null ? !_3.equals(tuple7._3) : tuple7._3 != null) return false;
		if(_4 != null ? !_4.equals(tuple7._4) : tuple7._4 != null) return false;
		if(_5 != null ? !_5.equals(tuple7._5) : tuple7._5 != null) return false;
		if(_6 != null ? !_6.equals(tuple7._6) : tuple7._6 != null) return false;
		return _7 != null ? _7.equals(tuple7._7) : tuple7._7 == null;

	}

	@Override
	public int hashCode() {
		int result = _1 != null ? _1.hashCode() : 0;
		result = 31 * result + (_2 != null ? _2.hashCode() : 0);
		result = 31 * result + (_3 != null ? _3.hashCode() : 0);
		result = 31 * result + (_4 != null ? _4.hashCode() : 0);
		result = 31 * result + (_5 != null ? _5.hashCode() : 0);
		result = 31 * result + (_6 != null ? _6.hashCode() : 0);
		result = 31 * result + (_7 != null ? _7.hashCode() : 0);
		return result;
	}

	public <R> R map(Function7<T1, T2, T3, T4, T5, T6, T7, R> map) {
		return map.apply(_1, _2, _3, _4, _5, _6, _7);
	}

	public <R1> Tuple7<R1, T2, T3, T4,T5,T6,T7> map1(Function<T1, R1> map) {
		return Tuple7.of(map.apply(_1), _2, _3, _4,_5,_6,_7);
	}

	public <R2> Tuple7<T1, R2, T3, T4,T5,T6,T7> map2(Function<T2, R2> map) {
		return Tuple7.of(_1, map.apply(_2), _3, _4,_5,_6,_7);
	}

	public <R3> Tuple7<T1, T2, R3, T4,T5,T6,T7> map3(Function<T3, R3> map) {
		return Tuple7.of(_1, _2, map.apply(_3), _4,_5,_6,_7);
	}

	public <R4> Tuple7<T1, T2, T3, R4,T5,T6,T7> map4(Function<T4, R4> map) {
		return Tuple7.of(_1, _2, _3, map.apply(_4),_5,_6,_7);
	}
	public <R5> Tuple7<T1, T2, T3, T4,R5,T6,T7> map5(Function<T5, R5> map) {
		return Tuple7.of(_1, _2, _3,_4, map.apply(_5),_6,_7);
	}
	public <R6> Tuple7<T1, T2, T3, T4,T5,R6,T7> map6(Function<T6, R6> map) {
		return Tuple7.of(_1, _2, _3,_4,_5, map.apply(_6),_7);
	}
	public <R7> Tuple7<T1, T2, T3, T4,T5,T6,R7> map7(Function<T7, R7> map) {
		return Tuple7.of(_1, _2, _3,_4,_5, _6, map.apply(_7));
	}

}
