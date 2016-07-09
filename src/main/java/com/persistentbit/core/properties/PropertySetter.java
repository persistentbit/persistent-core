package com.persistentbit.core.properties;
import java.lang.reflect.Type;

/**
 * @author Peter Muys
 * @since 23/10/2015
 */
public interface PropertySetter
{
    void set(Object container, Object value);

    Type getPropertyType();
    Class<?> getPropertyClass();
}
