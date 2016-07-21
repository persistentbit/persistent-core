package com.persistentbit.core.properties;
import java.lang.reflect.Type;

/**
 * Interface for setting a property on an object.<br>
 * @see PropertySetterField
 * @see PropertySetterMethod
 */
public interface PropertySetter
{
    /**
     * Set the provided value as a property on the provided container
     * @param container The container
     * @param value The value to set
     */
    void set(Object container, Object value);

    /**
     *
     * @return The expected type of the property to set
     */
    Type getPropertyType();

    /**
     *
     * @return The expected Calss of the property to set
     */
    Class<?> getPropertyClass();
}
