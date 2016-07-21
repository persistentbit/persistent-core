package com.persistentbit.core.properties;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Get the property of an object by invoking a provided {@link Method}.<br>
 * @see PropertyGetter
 * @see PropertyGetterField
 */
public class PropertyGetterMethod  implements PropertyGetter
{
    private final Method method;

    public PropertyGetterMethod(Method method)
    {
        this.method = method;
    }

    @Override
    public Object get(Object container)
    {
        try{
            return method.invoke(container);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Type getPropertyType()
    {
        return method.getGenericReturnType();
    }

    @Override
    public Class<?> getPropertyClass()
    {
        return method.getReturnType();
    }
}
