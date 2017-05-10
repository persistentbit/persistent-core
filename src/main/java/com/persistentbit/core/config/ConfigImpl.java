package com.persistentbit.core.config;

import com.persistentbit.core.result.Result;

import java.util.function.Supplier;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 10/05/2017
 */
public class ConfigImpl<T> {
    private final Supplier<Result<T>> supplier;
    private Result<T> value;

    public ConfigImpl(Supplier<Result<T>> supplier) {
        this.supplier = supplier;
    }
    public ConfigImpl(T value){
        this(()-> Result.success(value));
    }

    public synchronized Result<T> get(){
        if(value == null){
            value = supplier.get();
        }
        return value;
    }
    Result<T> get(T defaultValue){
        Result<T> res = get();
        if(res.isEmpty()){
            return Result.success(defaultValue);
        }
        return res;
    }
}
