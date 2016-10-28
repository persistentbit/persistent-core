package com.persistentbit.core.tuples;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.function.Function5;
import com.persistentbit.core.properties.FieldNames;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author Peter Muys
 * @since 30/09/2016
 */
public class Tuple5<T1, T2, T3, T4, T5> implements Comparable<Tuple5<T1, T2, T3, T4, T5>>, Serializable{

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

  @FieldNames(
	names = {"_1", "_2", "_3", "_4", "_5"}
  )
  public Tuple5(@Nullable T1 v1, @Nullable T2 v2, @Nullable T3 v3, @Nullable T4 v4, @Nullable T5 v5) {
	this._1 = v1;
	this._2 = v2;
	this._3 = v3;
	this._4 = v4;
	this._5 = v5;
  }

  public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> of(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
	return new Tuple5<>(v1, v2, v3, v4, v5);
  }


  public String toString() {
	return "(" + this._1 + "," + this._2 + ", " + this._3 + "," + this._4 + "," + this._5 + ")";
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

  <T6> Tuple6<T1, T2, T3, T4, T5, T6> add(T6 v6) {
	return Tuple6.of(_1, _2, _3, _4, _5, v6);
  }

  @Override
  @SuppressWarnings({"unchecked", "ConstantConditions"})
  public int compareTo(Tuple5<T1, T2, T3, T4, T5> o) {
	int r = this.dropLast().compareTo(o.dropLast());
	if(r != 0) {
	  return r;
	}
	else {
	  if(this._5 == null) {
		return o._5 == null ? 0 : -1;
	  }
	  return ((Comparable) this._5).compareTo(o._5);
	}
  }

  public Tuple4<T1, T2, T3, T4> dropLast() {
	return Tuple4.of(_1, _2, _3, _4);
  }

  public Tuple5<T1, T2, T3, T4, T5> with_1(T1 value) {
	return new Tuple5<>(value, this._2, this._3, this._4, this._5);
  }

  public Tuple5<T1, T2, T3, T4, T5> with_2(T2 value) {
	return new Tuple5<>(this._1, value, this._3, this._4, this._5);
  }

  public Tuple5<T1, T2, T3, T4, T5> with_3(T3 value) {
	return new Tuple5<>(this._1, this._2, value, this._4, this._5);
  }

  public Tuple5<T1, T2, T3, T4, T5> with_4(T4 value) {
	return new Tuple5<>(this._1, this._2, this._3, value, this._5);
  }

  public Tuple5<T1, T2, T3, T4, T5> with_5(T5 value) {
	return new Tuple5<>(this._1, this._2, this._3, this._4, value);
  }


  @Override
  public boolean equals(Object o) {
	if(this == o) return true;
	if(o == null || getClass() != o.getClass()) return false;

	Tuple5<?, ?, ?, ?, ?> tuple5 = (Tuple5<?, ?, ?, ?, ?>) o;

	if(_1 != null ? !_1.equals(tuple5._1) : tuple5._1 != null) return false;
	if(_2 != null ? !_2.equals(tuple5._2) : tuple5._2 != null) return false;
	if(_3 != null ? !_3.equals(tuple5._3) : tuple5._3 != null) return false;
	if(_4 != null ? !_4.equals(tuple5._4) : tuple5._4 != null) return false;
	return _5 != null ? _5.equals(tuple5._5) : tuple5._5 == null;

  }

  @Override
  public int hashCode() {
	int result = _1 != null ? _1.hashCode() : 0;
	result = 31 * result + (_2 != null ? _2.hashCode() : 0);
	result = 31 * result + (_3 != null ? _3.hashCode() : 0);
	result = 31 * result + (_4 != null ? _4.hashCode() : 0);
	result = 31 * result + (_5 != null ? _5.hashCode() : 0);
	return result;
  }

  public <R> R map(Function5<T1, T2, T3, T4, T5, R> map) {
	return map.apply(_1, _2, _3, _4, _5);
  }
}
