package com.persistentbit.core.properties;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author Peter Muys
 * @since 23/10/2015
 */
public class PropertyGetterField implements PropertyGetter
{
    private final Field field;
    public PropertyGetterField(Field f){
        this.field = f;
    }

    @Override
    public Object get(Object container)
    {
        try{
            return field.get(container);
        }catch (Exception e){
            throw new RuntimeException("Error getting property value " + field + " from " +  container,e);
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
