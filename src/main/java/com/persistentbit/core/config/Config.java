package com.persistentbit.core.config;

import com.persistentbit.core.result.Result;
import com.persistentbit.core.validation.Validator;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a Config setting.<br>
 * Typically read from a config file or a database.<br>
 * A Config setting can be watched for change.<br>
 *
 * @author Peter Muys
 * @since 10/05/2017
 */
public interface Config<T>{
    /**
     * Get the value.
     * @return The value as a {@link Result}
     */
    Result<T> get();

    /**
     * Add a change event listener
     * @param changeWatcher with the config and previous value as parameters
     */
    Config<T> watch(BiConsumer<Config<T>, Result<T>> changeWatcher);

    /**
     * Get the value or a defaultvalue if this setting is empty
     * @param defaultValue The default value on empty
     * @return The Result
     */
    default Result<T> get(T defaultValue){
        Result<T> res = get();
        if(res.isEmpty()){
            return Result.success(defaultValue);
        }
        return res;
    }

    /**
     * Info about this setting or an empty string
     * @return The info
     */
    String getInfo();

    String getName();

    /**
     * Get the validator for this setting.
     * Default value is a {@link com.persistentbit.core.validation.OKValidator}
     * @return The validator.
     */
    Validator<T> getValidator();

    /**
     * Register a new validator for this setting
     * @param validator The validator
     * @return this item.
     */
    Config<T> setValidator(Validator<T> validator);

    default <R> Config<R> mapped(Function<T,Result<R>> mapper){
        return new MappedConfig<>(this,getName(),mapper,getInfo(),Validator.ok());
    }
}
