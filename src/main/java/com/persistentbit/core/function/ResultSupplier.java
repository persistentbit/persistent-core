package com.persistentbit.core.function;

import com.persistentbit.core.result.Result;

import java.util.function.Supplier;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 16/05/2017
 */
public interface ResultSupplier<T> extends Supplier<Result<T>> {
}
