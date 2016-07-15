package com.persistentbit.core.mappers;

import com.persistentbit.core.collections.PMap;

import java.util.function.Function;

/**
 * @author Peter Muys
 * @since 15/07/2016
 */
public class ObjectMapperRegistry implements Function<Class,ObjectMapper> {
    private final Function<Class,ObjectMapper>    defaultMapperCreator;
    private PMap<Class,ObjectMapper>        mappers = PMap.empty();

    public ObjectMapperRegistry(Function<Class,ObjectMapper> defaultMapperCreator){
        this.defaultMapperCreator = defaultMapperCreator;
    }

    @Override
    public ObjectMapper apply(Class aClass) {
        ObjectMapper mapper = mappers.get(aClass);
        if(mapper == null){
            mapper = defaultMapperCreator.apply(aClass);
            mappers = mappers.put(aClass,mapper);
        }
        return mapper;
    }
    public ObjectMapperRegistry register(Class cls, ObjectMapper mapper){
        mappers = mappers.put(cls,mapper);
        return this;
    }
}
