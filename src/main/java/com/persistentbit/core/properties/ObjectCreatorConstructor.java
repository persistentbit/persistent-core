package com.persistentbit.core.properties;
import java.lang.reflect.Constructor;

/**
 * @author Peter Muys
 * @since 23/10/2015
 */
public class ObjectCreatorConstructor implements ObjectCreator
{
    private final Class<?> cls;
    private final Constructor<?> constructor;


    public ObjectCreatorConstructor(Class<?> cls){
        this.cls = cls;
        if(cls == null){
            throw new NullPointerException("cls is null");
        }
        Constructor<?> c;
        try{
            c = cls.getDeclaredConstructor(cls);
            c.setAccessible(true);
        }catch (NoSuchMethodException e){
            c = null;
        }
        constructor = c;
    }



    @Override
    public Object create()
    {
        try{
            if(constructor == null){
                return cls.newInstance();
            }
            return constructor.newInstance();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
