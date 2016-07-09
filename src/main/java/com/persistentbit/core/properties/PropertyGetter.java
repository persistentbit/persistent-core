package com.persistentbit.core.properties;
import java.lang.reflect.Type;

/**
 * @author Peter Muys
 * @since 23/10/2015
 */
public interface PropertyGetter
{
    Object get(Object container);
    Type getPropertyType();
    Class<?> getPropertyClass();
}
