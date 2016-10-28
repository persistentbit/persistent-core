package com.persistentbit.core.lenses;

import java.util.function.Function;

/**
 * Basic {@link Lens} implementation using Set and Get lambda functions.<br>
 *
 * @author Peter Muys
 * @since 20/06/2016
 */
public class LensImpl<P, C> implements Lens<P, C>{

  private final Function<P, C> getter;
  private final Setter<P, C>   setter;

  public LensImpl(Function<P, C> getter, Setter<P, C> setter) {
	this.getter = getter;
	this.setter = setter;
  }

  @Override
  public C get(P parent) {
	if(parent == null) {
	  return null;
	}
	return getter.apply(parent);
  }

  @Override
  public P set(P parent, C client) {
	return setter.set(parent, client);
  }

  @FunctionalInterface
  public interface Setter<P, C>{

	P set(P parent, C child);
  }

}
