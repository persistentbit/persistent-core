package com.persistentbit.core.mappers;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.function.NamedConsumer;
import com.persistentbit.core.function.NamedSupplier;

/**
 * User: petermuys
 * Date: 15/07/16
 * Time: 17:15
 */
public class MappedProperties implements NamedSupplier<Object>,NamedConsumer<Object>{
    private PMap<String,Object> map = PMap.empty();



    @Override
    public void accept(String name, Object value) {
        map = map.put(name,value);
    }

    @Override
    public Object apply(String name) {
        return map.get(name);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
