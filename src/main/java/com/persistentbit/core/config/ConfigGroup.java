package com.persistentbit.core.config;

import com.persistentbit.core.OK;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.POrderedMap;
import com.persistentbit.core.keyvalue.NamedValue;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.UNumber;
import com.persistentbit.core.utils.UReflect;
import com.persistentbit.core.utils.UString;
import com.persistentbit.core.validation.OKValidator;
import com.persistentbit.core.validation.Validator;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 10/05/2017
 */
public class ConfigGroup {
    public class Property<T> extends BaseValueClass implements Config<T> {
        private String name;
        private Class type;
        private String info;
        private Validator<T> validator;
        private Result<T> value;
        private PList<BiConsumer<Config,Result<T>>> watchers;
        public Property(String name, Class type, String info, Result<T> value,PList<BiConsumer<Config,Result<T>>> watchers,Validator<T> validator) {
            this.name = name;
            this.type = type;
            this.info = info;
            this.value = value;
            this.watchers = watchers;
            this.validator = validator;
        }

        @Override
        public String toString() {
            String infoString = info.isEmpty() ? "" : "  ..." + info;
            return UReflect.typeToSimpleString(type) +" " + name + " = " + value + infoString;
        }


        @Override
        public Result<T> get() {
            return value;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public void setValidator(Validator<T> validator) {
            this.validator = validator;
        }

        public String getName() {
            return name;
        }

        public String getInfo() {
            return info;
        }

        public Validator<T> getValidator() {
            return validator;
        }

        @Override
        public synchronized void watch(BiConsumer<Config,Result<T>> changeWatcher) {
            watchers = watchers.plus(changeWatcher);
        }
        public synchronized Result<OK> set(Result<T> newValue){
            return Result.function(newValue).code(l -> {
                Result<T> oldValue = value;
                value = newValue
                        .flatMap(t -> validator.validateToResult(name,t));

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
    public ConfigGroup() {
        this(POrderedMap.empty());
    }


    public <T> Property<T> add(String name, Class<T> type, T defaultValue, String info){
        Property<T> prop = new Property<>(name,type,info,Result.result(defaultValue),PList.empty(), OKValidator.inst());
        this.properties = this.properties.put(name,prop);
        return prop;
    }

    public Property<Integer> addInt(String name, Integer defaultValue, String info){
        return add(name,Integer.class, defaultValue, info);
    }
    public Property<Long> addLong(String name, Long defaultValue, String info){
        return add(name,Long.class, defaultValue, info);
    }
    public Property<String> addString(String name, String defaultValue, String info){
        return add(name,String.class, defaultValue, info);
    }
    public Property<Boolean> addBoolean(String name, Boolean defaultValue, String info){
        return add(name,Boolean.class, defaultValue, info);
    }
    public <E extends Enum> Property<E> addEnum(String name,Class<E> cls,  E defaultValue, String info){
        return add(name,cls, defaultValue, info);
    }


    public Result<OK> set(String name, Result<?> resValue){
        return Result.function(name,resValue).code(l -> {
            Property cfg = this.properties.getOrDefault(name, null);
            if(cfg == null){
                return Result.failure("Unknown property: '" + name + "'");
            }
            Result<?> mappedRes = resValue.flatMap(value -> {
                if(Enum.class.isAssignableFrom(cfg.type)){
                    if(value instanceof String == false){
                        return Result.failure("Expected an enum name, got " + value);
                    }
                    Result res = Result.noExceptions(()-> Enum.valueOf(cfg.type,value.toString()));
                    return res;
                }
                if(cfg.type.isAssignableFrom(value.getClass())){
                    return resValue;
                }
                if(Number.class.isAssignableFrom(value.getClass())){
                    //We have a number type...
                    return UNumber.convertTo((Number)value,cfg.type);
                }
                return Result.failure("Don't know how to convert " + value + " to " + UReflect.typeToSimpleString(cfg.type));
            });

            cfg.set(mappedRes);
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
                    Result<OK> setRes = set(nv.key,Result.result(nv.value));
                    if(setRes.isError()){
                        return setRes;
                    }
                }
            }
            return OK.result;
        });
    }

    @Override
    public String toString() {
        return properties.values().map(p -> p.toString()).toString(UString.NL);
    }
}
