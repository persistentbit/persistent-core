package com.persistentbit.core.experiments.config;

import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.UNumber;
import com.persistentbit.core.utils.UString;
import com.persistentbit.core.validation.Validator;

import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/05/2017
 */

public interface ConfigVar<T> {
    Result<T>    get();
    String getInfo();
    String getName();

    ConfigVar<T> set(Result<T> value);

    ConfigVar<T> setString(String value);
    Result<String> getStringValue();
    ConfigVar<T> setInfo(String info);

    default ConfigVar<T> validate(Validator<T> validator){
        return new MappedConfigVar<>(this,v -> validator.validateToResult(getName(),v), Result::result);
    }
    default <R> ConfigVar<R> mapValue(Function<T, Result<R>> mapTo, Function<R, Result<T>> mapFrom){
        return new MappedConfigVar<>(this,mapTo,mapFrom);
    }

    static MemConfigVar<Boolean> createBool(String name){
        return new MemConfigVar<>(name, UString::parseBoolean);
    }
    static MemConfigVar<String> createString(String name){
        return new MemConfigVar<>(name, Result::result);
    }
    static MemConfigVar<Integer> createInt(String name){
        return new MemConfigVar<>(name, UNumber::parseInt);
    }
    static MemConfigVar<Long> createLong(String name){
        return new MemConfigVar<>(name, UNumber::parseLong);
    }
}
