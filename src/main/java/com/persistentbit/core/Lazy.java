package com.persistentbit.core;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A Lazy Value Supplier.
 * When the lazy supplier is first called, the value is retrieved from the provide Supplier.
 * Once it has a value, than the value will be reused in the next calls to {@link #get()}
 *
 * @see Supplier
 */
public class Lazy<T> implements Supplier<T>{

  private final Supplier<T> supplier;
  private       T           value;
  private       boolean     gotValue;

  /**
   * Init with the master supplier
   *
   * @param supplier The master supplier
   */
  public Lazy(Supplier<T> supplier) {
	  this.supplier = Objects.requireNonNull(supplier);
  }

	/**
	 * Create a new Lazy value
	 *
	 * @param supplier The value supplier
	 * @param <R>      Type of the value
	 *
	 * @return a new Lazy Object
	 */
	public static <R> Lazy<R> code(Supplier<R> supplier) {
		return new Lazy<>(supplier);
  }

  /**
   * Get the value from the master supplier on the first call.<br>
   * Then reuse the value in all the later calls.
   *
   * @return The lazy value
   */
  @Override
  public synchronized T get() {
	if(gotValue == false) {
	  value = supplier.get();
	  gotValue = true;
	}
	return value;
  }
}
