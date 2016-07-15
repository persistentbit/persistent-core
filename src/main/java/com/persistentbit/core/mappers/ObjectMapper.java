package com.persistentbit.core.mappers;

import com.persistentbit.core.function.NamedConsumer;
import com.persistentbit.core.function.NamedSupplier;

/**
 * @author Peter Muys
 * @since 15/07/2016
 */
public interface ObjectMapper<OBJ> {
    void getProperties(OBJ obj,NamedConsumer<Object> properties);
    OBJ create(NamedSupplier<Object> properties);
}
