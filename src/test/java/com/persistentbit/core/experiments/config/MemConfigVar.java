package com.persistentbit.core.experiments.config;

import com.persistentbit.core.result.Result;
import org.apache.commons.lang.NullArgumentException;

import java.util.Objects;
import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/05/2017
 */
public class MemConfigVar<T> implements ConfigVar<T> {
    private final String name;
    private final Function<String,Result<T>> fromString;
    private final Function<T,Result<String>> toString;
    private String info;
    private Result<T> value;

    public MemConfigVar(String name, Function<String, Result<T>> fromString, Function<T, Result<String>> toString, String info) {
        this.name = name;
        this.fromString = fromString;
        this.toString = toString;
        this.info = info;
        this.value = Result.empty("Config var " + name + " not set");
    }
    public MemConfigVar(String name, Function<String, Result<T>> fromString){
        this(name,fromString,val -> Result.success(val.toString()),"");
    }

    @Override
    public Result<T> get() {
        return value;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MemConfigVar<T> setInfo(String info) {
        this.info = Objects.requireNonNull(info);
        return this;
    }

    @Override
    public MemConfigVar<T> set(Result<T> value) {
        if(value == null){
            this.value = Result.failure(new NullArgumentException("Can't set to null:" + name));
        } else {
            this.value = value;
        }
        return this;
    }

    @Override
    public MemConfigVar<T> setString(String value) {
        return set(fromString.apply(value));
    }

    @Override
    public Result<String> getStringValue() {
        return get().flatMap(toString);
    }


}
