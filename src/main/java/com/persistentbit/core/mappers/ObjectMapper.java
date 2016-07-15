package com.persistentbit.core.mappers;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.function.NamedConsumer;
import com.persistentbit.core.function.NamedSupplier;

/**
 * @author Peter Muys
 * @since 15/07/2016
 */
public interface ObjectMapper<OBJ> {
    void getProperties(String name, OBJ obj,NamedConsumer<Object> result);
    OBJ create(String name,NamedSupplier<Object> properties);
}
