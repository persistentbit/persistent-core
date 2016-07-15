package com.persistentbit.core.mappers;

import com.persistentbit.core.Tuple2;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.function.NamedConsumer;
import com.persistentbit.core.function.NamedSupplier;
import com.persistentbit.core.utils.ImTools;

import java.util.function.Function;

/**
 * @author Peter Muys
 * @since 15/07/2016
 */
public class ObjectMapperDefault<OBJ> implements ObjectMapper<OBJ>{
    private final ImTools<OBJ> im;

    private PMap<String,ObjectMapper<Object>> propMappers = PMap.empty();
    private Function<Class,ObjectMapper>  mapperSupplier;


    public ObjectMapperDefault(Class<OBJ> cls, Function<Class,ObjectMapper> mapperSupplier){
        this.mapperSupplier = mapperSupplier;
        this.im = ImTools.get(cls);
    }

    public ObjectMapperDefault<OBJ> addAllFields(){
        return addAllFieldsExcept();
    }

    public ObjectMapperDefault<OBJ> addAllFieldsExcept(String...fieldNames){
        PSet<String> exclude = PStream.from(fieldNames).pset();
        //Create default Property mappers
        propMappers = im.getFieldGetters().filter(g -> exclude.contains(g.propertyName) == false).with(propMappers, (pm,t) -> {
            ObjectMapper fm = mapperSupplier.apply(t.field.getType());
            return pm.put(t.propertyName,fm);
        });
        return this;
    }



    private ObjectMapper<Object> fieldMapper(String fieldName){
        ObjectMapper<Object> master = propMappers.get(fieldName);
        if(master == null){
            throw new IllegalArgumentException("No field with name '" + fieldName + "' found. Check the spelling or check that you have called addFields first.");
        }
        return master;
    }

    public ObjectMapperDefault<OBJ> rename(String fieldName, String propertyName){
        ObjectMapper<Object> master = fieldMapper(fieldName);


        propMappers = propMappers.put(fieldName,new ObjectMapper<Object>(){
            @Override
            public void getProperties(String name, Object o, NamedConsumer result) {
                master.getProperties(propertyName,o,result);
            }

            @Override
            public Object create(String name, NamedSupplier properties) {
                return master.create(propertyName,properties);
            }
        });
        return this;
    }

    public ObjectMapperDefault<OBJ> ignore(String fieldName,Object defaultValue){
        propMappers = propMappers.put(fieldName, new ObjectMapper<Object>() {
            @Override
            public void getProperties(String name, Object o, NamedConsumer<Object> result) {

            }

            @Override
            public Object create(String name, NamedSupplier<Object> properties) {
                return defaultValue;
            }
        });
        return this;
    }

    public ObjectMapperDefault<OBJ> prefix(String fieldName,String prefix){
        ObjectMapper<Object> master = fieldMapper(fieldName);

        propMappers = propMappers.put(fieldName,new ObjectMapper<Object>(){
            @Override
            public void getProperties(String name, Object o, NamedConsumer result) {
                master.getProperties(name, o, new NamedConsumer<Object>() {
                    @Override
                    public void accept(String name, Object value) {
                        result.accept(prefix + name,value);
                    }
                });
            }

            @Override
            public Object create(String name, NamedSupplier properties) {
                return master.create(name, new NamedSupplier<Object>() {
                    @Override
                    public Object apply(String name) {
                        return properties.apply(prefix+name);
                    }
                });
            }
        });
        return this;
    }


    @Override
    public void getProperties(String name, OBJ obj,NamedConsumer<Object> result) {

        propMappers.forEach(t ->
            t._2.getProperties(t._1,im.get(obj,t._1),result)
        );
    }

    @Override
    public OBJ create(String name,NamedSupplier<Object> properties) {
        PMap<String,Object> props = propMappers.mapKeyValues(t-> Tuple2.of(t._1,t._2.create(t._1,properties)));
        return im.createNew(props);
    }
}
