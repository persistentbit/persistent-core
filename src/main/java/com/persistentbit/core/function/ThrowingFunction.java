package com.persistentbit.core.function;

import com.persistentbit.core.result.Result;

import java.util.function.Function;

/**
 * A Function that can throw an {@link Exception}
 *
 * @author petermuys
 * @since 14/01/17
 */
@FunctionalInterface
public interface ThrowingFunction<P, R, E extends Exception>{

	R apply(P value) throws E;

	default Result<R> applyResult(P value){
		return Result.noExceptions(() -> apply(value));
	}

	default Function<P,R> toNonChecked() {
		return p -> {
			try{
				return apply(p);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		};
	}
	static <P,R> Function<P,R> createNonChecked(ThrowingFunction<P,R,Exception> fun){
		return fun.toNonChecked();
	}
}
