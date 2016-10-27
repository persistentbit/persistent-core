package com.persistentbit.core;


import com.persistentbit.core.properties.FieldNames;
import com.persistentbit.core.tuples.Tuple2;

import java.util.Objects;

/**
 * A {@link Tuple2} with non null values.
 *
 * @author Peter Muys
 */

public class Pair<L, R> extends Tuple2<L, R>{

  @FieldNames(names = {"_1", "_2"})
  public Pair(L _1, R _2) {
	super(
	  Objects.requireNonNull(_1),
	  Objects.requireNonNull(_2)
	);
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