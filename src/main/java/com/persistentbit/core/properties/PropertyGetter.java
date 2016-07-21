package com.persistentbit.core.properties;
import java.lang.reflect.Type;

/**
 * Interface for getting a property from an Object.<br>
 * @see PropertyGetterField
 * @see PropertyGetterMethod
 */
public interface PropertyGetter
{

    Object get(Object container);
    Type getPropertyType();
    Class<?> getPropertyClass();
}
