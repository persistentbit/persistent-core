package com.persistentbit.core.config;

import com.persistentbit.core.OK;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.POrderedMap;
import com.persistentbit.core.keyvalue.NamedValue;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 10/05/2017
 */
public class ConfigGroup {
    private class Property<T> extends BaseValueClass implements Config<T> {
        public final String name;
        public final Class type;
        public final String info;
        private Result<T> value;
        private PList<BiConsumer<Config,Result<T>>> watchers;
        public Property(String name, Class type, String info, Result<T> value,PList<BiConsumer<Config,Result<T>>> watchers) {
            this.name = name;
            this.type = type;
            this.info = info;
            this.value = value;
            this.watchers = watchers;
        }

        @Override
        public Result<T> get() {
            return value;
        }

        @Override
        public synchronized void watch(BiConsumer<Config,Result<T>> changeWatcher) {
            watchers = watchers.plus(changeWatcher);
        }
        public synchronized Result<OK> set(Result newValue){
            return Result.function(newValue).code(l -> {
                Result<T> oldValue = value;
                value = newValue;
                if(oldValue.equals(value) == false){
                    watchers.forEach(c -> c.accept(this,oldValue));
                }
                return OK.result;
            });
        }
    }

    private POrderedMap<String, Property> properties;

    public ConfigGroup(POrderedMap<String, Property> properties) {
        this.properties = Objects.requireNonNull(properties);
    }

    public <T> Property<T> add(String name, Class<T> type, String info, T defaultValue){
        Property<T> prop = new Property<>(name,type,info,Result.result(defaultValue),PList.empty());
        this.properties = this.properties.put(name,prop);
        return prop;
    }

    public Result<OK> set(String name, Result<?> value){
        return Result.function(name,value).code(l -> {
            Property cfg = this.properties.getOrDefault(name, null);
            if(cfg == null){
                return Result.failure("Unknown property: '" + name + "'");
            }
            cfg.set(value);
            return OK.result;
        });
    }

    public <T> Result<Config<T>> get(String name){
        Result res = properties.getResult(name);
        return (Result<Config<T>>)res;
    }

    public Result<OK> load(Source source){
        return Result.function(source.position).code(l -> {
            ParseResult<PList<NamedValue<Object>>> res = ValueParser.parseAll().parse(source);
            if(res.isFailure()){
                return res.asResult().map(v -> null);
            }
            for(NamedValue<Object> nv : res.getValue()){
                l.info("Setting property '" + nv.key + "'");
                if(get(nv.key).isEmpty()){
                    l.warning("Unknown property: '" + nv.key);
                } else {
                    Result<OK> setRes = properties.get(nv.key).set(Result.result(nv.value));
                    if(setRes.isError()){
                        return setRes;
                    }
                }
            }
            return OK.result;
        });
    }

}
