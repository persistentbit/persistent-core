package com.persistentbit.core.experiments.config;

import com.persistentbit.core.result.Result;

import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/05/2017
 */
public class MappedConfigVar<T,R> implements ConfigVar<R> {
    private final ConfigVar<T> org;
    private final Function<T,Result<R>> fromOrg;
    private final Function<R, Result<T>> toOrg;

    public MappedConfigVar(ConfigVar<T> org, Function<T, Result<R>> fromOrg, Function<R, Result<T>> toOrg) {
        this.org = org;
        this.fromOrg = fromOrg;
        this.toOrg = toOrg;
    }

    @Override
    public Result<R> get() {
        return org.get().flatMap(fromOrg);
    }

    @Override
    public String getInfo() {
        return org.getInfo();
    }

    @Override
    public String getName() {
        return org.getName();
    }

    @Override
    public ConfigVar<R> set(Result<R> value) {
        org.set(value.flatMap(toOrg));
        return this;
    }

    @Override
    public ConfigVar<R> setInfo(String info) {
        org.setInfo(info);
        return this;
    }

    @Override
    public ConfigVar<R> setString(String value) {
        org.setString(value);
        return this;
    }

    @Override
    public Result<String> getStringValue() {
        return org.getStringValue();
    }
}
