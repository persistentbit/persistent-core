package com.persistentbit.core.properties;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author Peter Muys
 * @since 23/10/2015
 */
public class PropertySetterField implements PropertySetter
{
    private final Field field;

    public PropertySetterField(Field field)
    {
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    public void set(Object container, Object value)
    {
        try{
            field.set(container,value);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Type getPropertyType()
    {
        return field.getGenericType();
    }

    @Override
    public Class<?> getPropertyClass()
    {
        return field.getType();
    }
}
