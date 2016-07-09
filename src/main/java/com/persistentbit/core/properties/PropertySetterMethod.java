package com.persistentbit.core.properties;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author Peter Muys
 * @since 23/10/2015
 */
public class PropertySetterMethod implements PropertySetter
{
    private final Method method;

    public PropertySetterMethod(Method method)
    {
        this.method = method;
        method.setAccessible(true);
    }

    @Override
    public void set(Object container, Object value)
    {
        try{
            method.invoke(container,value);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Type getPropertyType()
    {
        return  method.getGenericParameterTypes()[0];
    }

    @Override
    public Class<?> getPropertyClass()
    {
        return method.getParameterTypes()[0];
    }
}
