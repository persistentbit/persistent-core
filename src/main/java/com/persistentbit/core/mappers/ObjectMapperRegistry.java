package com.persistentbit.core.mappers;

import com.persistentbit.core.collections.PMap;

import java.util.function.Function;

/**
 * A registry of known {@link ObjectMapper} instances.<br>
 * @author Peter Muys
 * @since 15/07/2016
 */
public class ObjectMapperRegistry implements Function<Class,ObjectMapper> {
    private final Function<Class,ObjectMapper>    defaultMapperCreator;
    private PMap<Class,ObjectMapper>        mappers = PMap.empty();


    public ObjectMapperRegistry(){
        this.defaultMapperCreator = (cls -> new ObjectMapperDefault(cls,this));
        ObjectMapper pon = PassOnObjectMapper.inst;
        register(int.class,pon);
        register(Integer.class,pon);
        register(long.class,pon);
        register(Long.class,pon);
        register(float.class,pon);
        register(Float.class,pon);
        register(double.class,pon);
        register(Double.class,pon);
        register(short.class,pon);
        register(Short.class,pon);
        register(boolean.class,pon);
        register(Boolean.class,pon);
        register(String.class,pon);
    }

    /**
     * Return a mapper for the provided Class.<br>
     * If there is no known mapper then a new Mapper will be created and registered.<br>
     * @param aClass The class to mapp
     * @return The Mapper of the provided class
     */
    @Override
    public ObjectMapper apply(Class aClass) {
        ObjectMapper mapper = mappers.get(aClass);
        if(mapper == null){
            mapper = defaultMapperCreator.apply(aClass);
            mappers = mappers.put(aClass,mapper);
        }
        return mapper;
    }

    public <OBJ> ObjectMapperDefault<OBJ> registerDefault(Class<OBJ> cls){
        ObjectMapperDefault<OBJ> def = new ObjectMapperDefault<OBJ>(cls,this);
        register(cls,def);
        return def;
    }

    /**
     * Register a new Object mapper for the provided Class.<br>
     * @param cls The class to register the mapper for
     * @param mapper The mapper for the provide Class
     * @return This registry
     */
    public ObjectMapperRegistry register(Class cls, ObjectMapper mapper){
        mappers = mappers.put(cls,mapper);
        return this;
    }
}
