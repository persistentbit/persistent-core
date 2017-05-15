package com.persistentbit.core.config;

import com.persistentbit.core.result.Result;
import com.persistentbit.core.validation.Validator;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/05/2017
 */
public class MappedConfig<V,R> implements Config<R> {
    private final Config<V> orgConfig;
    private final Function<V,Result<R>> mapper;
    private final String info;
    private final String name;
    private Validator<R> validator;


    public MappedConfig(Config<V> orgConfig, String name, Function<V, Result<R>> mapper, String info, Validator<R> validator) {
        this.orgConfig = orgConfig;
        this.mapper = mapper;
        this.info = info;
        this.name = name;
        this.validator = validator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Result<R> get() {
        return orgConfig.get()
                .flatMap(mapper)
                .flatMap(t -> validator.validateToResult(name,t));
    }

    @Override
    public Config<R> watch(BiConsumer<Config<R>, Result<R>> changeWatcher) {
       orgConfig.watch((conf, prevValue) -> {
          Result<R> prevResult = prevValue.flatMap(mapper::apply);
          changeWatcher.accept(this, prevResult);
       });
       return this;
    }


    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public Validator<R> getValidator() {
        return validator;
    }

    @Override
    public Config<R> setValidator(Validator<R> validator) {
        this.validator = validator;
        return this;
    }
}
