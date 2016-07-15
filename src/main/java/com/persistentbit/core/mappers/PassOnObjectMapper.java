package com.persistentbit.core.mappers;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.function.NamedConsumer;
import com.persistentbit.core.function.NamedSupplier;

/**
 * User: petermuys
 * Date: 15/07/16
 * Time: 14:32
 */
public class PassOnObjectMapper implements ObjectMapper<Object>{

    static public final ObjectMapper<Object>    inst = new PassOnObjectMapper();

    private PassOnObjectMapper() {

    }


    @Override
    public void getProperties(String name,Object value,NamedConsumer<Object> result) {
        result.accept(name,value);
    }

    @Override
    public Object create(String name,NamedSupplier<Object> properties) {
        return properties.apply(name);
    }
}
