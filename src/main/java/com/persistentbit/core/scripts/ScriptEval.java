package com.persistentbit.core.scripts;

import com.persistentbit.core.result.Result;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 7/02/17
 */
@FunctionalInterface
public interface ScriptEval extends Function<String, Result<Object>>{

}
