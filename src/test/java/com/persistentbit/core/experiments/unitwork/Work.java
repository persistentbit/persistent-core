package com.persistentbit.core.experiments.unitwork;

import com.persistentbit.core.result.Result;

/**
 * TODOC
 *
 * @author petermuys
 * @since 5/01/17
 */
@FunctionalInterface
public interface Work<CTX,R>{
	Result<R> apply(CTX context);
}
