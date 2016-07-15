package com.persistentbit.core.mappers;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.function.NamedConsumer;
import com.persistentbit.core.function.NamedSupplier;
import com.persistentbit.core.utils.ImTools;

/**
 * @author Peter Muys
 * @since 15/07/2016
 */
public class ObjectMapperImpl<OBJ> implements ObjectMapper<OBJ>{
    private final ImTools<OBJ> im;

    private PMap<String,ObjectMapper<Object>> propMappers;

    public ObjectMapperImpl(Class<OBJ> cls,ObjectMapperRegistry mappersRegistry){
        im = ImTools.get(cls);
    }

    @Override
    public void getProperties(OBJ obj, NamedConsumer<Object> properties) {
        propMappers.forEach(t -> {
            t._2.getProperties(im.get(obj,t._1),properties);
        });
    }

    @Override
    public OBJ create(NamedSupplier<Object> properties) {
        return null;
    }
}
