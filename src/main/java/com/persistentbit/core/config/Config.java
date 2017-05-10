package com.persistentbit.core.config;

import com.persistentbit.core.result.Result;

import java.util.function.BiConsumer;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 10/05/2017
 */

public interface Config<T>{
    Result<T> get();
    void watch(BiConsumer<Config, Result<T>> changeWatcher);

    default Result<T> get(T defaultValue){
        Result<T> res = get();
        if(res.isEmpty()){
            return Result.success(defaultValue);
        }
        return res;
    }

}
