package com.persistentbit.core.function;


import java.util.function.Function;

/**
 * A Supplier of named values
 *
 * @author Peter Muys
 * @since 15/07/2016
 */
@FunctionalInterface
public interface NamedSupplier<T> extends Function<String, T>{

  /**
   * Gets a result.
   *
   * @param name The name of the value
   *
   * @return a result
   */
  @Override
  @SuppressWarnings("AbstractMethodOverridesAbstractMethod")
  T apply(String name);
}
