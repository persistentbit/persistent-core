package com.persistentbit.core.lenses;

import java.util.function.Function;

/**
 * A Lens where the Setter returns the same parent
 * User: petermuys
 * Date: 3/03/16
 * Time: 18:39
 */
public class ReadOnlyLens<P, C> implements Lens<P, C>{

  private final Function<P, C> getter;

  public ReadOnlyLens(Function<P, C> getter) {
	this.getter = getter;
  }

  @Override
  public P set(P parent, C client) {
	return parent;
  }

  @Override
  public C get(P parent) {
	return getter.apply(parent);
  }
}
