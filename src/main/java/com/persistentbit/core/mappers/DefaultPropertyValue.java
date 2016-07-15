package com.persistentbit.core.mappers;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.function.NamedConsumer;
import com.persistentbit.core.function.NamedSupplier;

/**
 * User: petermuys
 * Date: 15/07/16
 * Time: 15:18
 */
public class DefaultPropertyValue<OBJ> implements ObjectMapper<OBJ>{
    private final ObjectMapper<OBJ> parent;
    private final String    propertyName;
    private final Object    defaultValue;
    private DefaultPropertyValue(ObjectMapper<OBJ> parent,String propertyName,Object defaultValue){
        this.parent = parent;
        this.propertyName = propertyName;
        this.defaultValue =defaultValue;
    }

    @Override
    public void getProperties(String name, OBJ obj, NamedConsumer<Object> result) {
        result.accept(propertyName,defaultValue);
        parent.getProperties(name,obj,result);
    }

    @Override
    public OBJ create(String name, NamedSupplier<Object> properties) {
        return parent.create(name, new NamedSupplier<Object>() {
            @Override
            public Object apply(String name) {
                Object res =properties.apply(name);
                if(res  == null && propertyName.equals(name)){
                    return defaultValue;
                }
                return res;
            }
        });
    }
}
