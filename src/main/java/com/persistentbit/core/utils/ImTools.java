package com.persistentbit.core.utils;



import com.persistentbit.core.NotNullable;
import com.persistentbit.core.Nullable;
import com.persistentbit.core.Pair;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.lenses.Lens;
import com.persistentbit.core.properties.FieldNames;
import com.persistentbit.core.properties.PropertyGetter;
import com.persistentbit.core.properties.PropertyGetterField;


import java.lang.reflect.*;
import java.util.*;


/**
 * This is a Immutable Objects utility class.<br>
 * This utility class uses reflection to provide multiple utility functions for Immutable Objects.<br>
 * Use {@link ImTools#get(Class)} to get an ImTools instance for a class.<br>
 *
 * @author Peter Muys
 * @since 3/03/2016
 */
public class ImTools<C> {
    static private Map<Class<?>,ImTools> sClassTools =   new HashMap<>();

    /**
     * Get an existing or create a new ImTools object for the provided class.<br>
     * The class should be of an immutable object<br>
     * @param cls The class of the immutable object
     * @param <C> Type of the class for ImTools instance
     * @return The ImTools instance for the provided class
     */
    static public synchronized <C> ImTools<C>    get(Class<C> cls) {
        ImTools<C> im = (ImTools<C>)sClassTools.get(cls);
        if(im == null){
            im = new ImTools<>(cls);
            sClassTools.put(cls,im);
        }
        return im;
    }



    public static class Getter {
        public final PropertyGetter getter;
        public final String propertyName;
        public final boolean isNullable;
        public final Field field;
        public Getter(PropertyGetter getter, String propertyName, boolean isNullable,Field f){
            this.getter = getter;
            this.propertyName = propertyName;
            this.isNullable = isNullable;
            this.field = f;
        }
        @Override
        public String toString()
        {
            return "FieldGetter[" + propertyName + "]";
        }
    }

    private PMap<String,Getter> getters = PMap.empty();
    private List<Getter> constructorProperties = new ArrayList<>();
    private Constructor<?> constructor;

    private final Class<C> cls;
    private C unitObject;


    private ImTools(Class<C> cls){

        this.cls = cls;
        List<Getter> allGetters = getFields();
        setBestConstructor(allGetters);
        if(constructor == null){
            throw new RuntimeException("Can't find constructor for " + this.cls.getName() + " with properties" + allGetters);
        }
        for(Getter g : allGetters){
            getters = getters.put(g.propertyName,g);
        }
    }

    /**
     * Get or create a unit object for the class C<br>
     * The unit object is created by the default constructor of class C<br>
     * Unit objects are cached and chared between invocations.<br>
     * @return The unit object
     */
    public C   unit() {
        if(unitObject != null){
            return unitObject;
        }
        try {
            Constructor ec = cls.getConstructor();
            unitObject = (C)ec.newInstance();
            return unitObject;

        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("No default constructor in " + cls.getName() + " for unit()");
        }
    }


    public C    createNew(PMap<String,Object> newProperties){
        Objects.requireNonNull(newProperties,"newProperties");
        Object[] params = new Object[constructorProperties.size()];
        for(int t=0; t<constructorProperties.size();t++){
            Getter g = constructorProperties.get(t);

            if(newProperties.containsKey(g.propertyName)){
                params[t] = newProperties.get(g.propertyName);
            } else {
                params[t] = null;
            }
        }
        try {
            return (C)constructor.newInstance(params);
        } catch (InstantiationException |IllegalAccessException |InvocationTargetException e) {
            throw new RuntimeException("Error constructing new " + cls.getName()  + " with newProperties " + newProperties);
        }
    }

    public PStream<Getter> getConstructorProperties() {
        return PStream.from(constructorProperties);
    }

    public C    copyWith(C orgObject, PMap<String,Object> newProperties){
        Objects.requireNonNull(orgObject,"orgObject");
        Objects.requireNonNull(newProperties,"newProperties");
        Object[] params = new Object[constructorProperties.size()];
        for(int t=0; t<constructorProperties.size();t++){
            Getter g = constructorProperties.get(t);

            if(newProperties.containsKey(g.propertyName)){
                params[t] = newProperties.get(g.propertyName);
            } else {
                params[t] = g.getter.get(orgObject);
            }
        }
        try {
            return (C)constructor.newInstance(params);
        } catch (InstantiationException |IllegalAccessException |InvocationTargetException e) {
            throw new RuntimeException("Error constructing new " + cls.getName() + " from original "+  orgObject + " with newProperties " + newProperties);
        }
    }

    public Object get(C container, String name){
        return getters.get(name).getter.get(container);
    }

    public C copyWith(C orgObject, String n1, Object v1){
        PMap<String,Object> m = new PMap<>();
        m = m.put(n1,v1);
        return copyWith(orgObject,m);
    }

    public C copyWith(C orgObject, String n1, Object v1, String n2, Object v2){
        PMap<String,Object> m = PMap.empty();
        m = m.put(n1,v1).put(n2,v2);
        return copyWith(orgObject,m);
    }

    public C copyWith(C orgObject, String n1, Object v1, String n2, Object v2, String n3, Object v3){
        PMap<String,Object> m = PMap.empty();
        m = m.put(n1,v1).put(n2,v2).put(n3,v3);
        return copyWith(orgObject,m);
    }



    private List<Getter> getFields(){
        Class<?> cls  = this.cls;
        List<Getter> result = new ArrayList<>();
        try{
            while(cls !=null && cls.equals(Object.class) == false){
                for(Field f : cls.getDeclaredFields()){
                    if(Modifier.isTransient(f.getModifiers())){
                        continue;
                    }
                    if(Modifier.isStatic(f.getModifiers())){
                        continue;
                    }
                    f.setAccessible(true);
                    Boolean isNullable = null;
                    if(f.getAnnotation(Nullable.class) != null) { isNullable = true; }
                    if(f.getAnnotation(NotNullable.class) != null) { isNullable = false;  }
                    if(isNullable == null){
                        isNullable =  checkGetterNullable(f);
                    }
                    result.add(new Getter(new PropertyGetterField(f),f.getName(),isNullable,f));
                }
                cls = cls.getSuperclass();
            }
            return result;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    public Optional<Method> getGetterMethod(String fieldName){
        Class<?> cls  = this.cls;
        String get1 = "get" + firstCharUppercase(fieldName);
        String get2 = "is" + firstCharUppercase(fieldName);


        while(cls != null && cls.equals(Object.class) == false){
            for(Method m : cls.getDeclaredMethods()){
                String name = m.getName();
                if(!name.equals(get1) &&  !name.equals(get2)){
                    continue;
                }
                return Optional.of(m);
            }
            cls = cls.getSuperclass();
        }
        return Optional.empty();
    }

    public Optional<Method> getWithMethod(Field f){
        Class<?> cls  = this.cls;
        String get1 = "with" + firstCharUppercase(f.getName());


        while(cls != null && cls.equals(Object.class) == false){
            for(Method m : cls.getDeclaredMethods()){
                String name = m.getName();
                if(!name.equals(get1)){
                    continue;
                }
                return Optional.of(m);
            }
            cls = cls.getSuperclass();
        }
        return Optional.empty();
    }

    private boolean checkGetterNullable(Field f) throws Exception {
        Class<?> cls  = this.cls;
        String get1 = "get" + firstCharUppercase(f.getName());
        String get2 = "is" + firstCharUppercase(f.getName());


        while(cls != null && cls.equals(Object.class) == false){
            for(Method m : cls.getDeclaredMethods()){
                String name = m.getName();
                if(!name.equals(get1) &&  !name.equals(get2)){
                    continue;
                }
                return Optional.class.equals(m.getReturnType());
            }
            cls = cls.getSuperclass();
        }
        return false;   //if not defined: assume not nullable
        //throw new RuntimeException("Don't know if " + f.getName() + " in " + this.cls.getName() + " is nullable or not nullable");
    }

    public PStream<Getter> getFieldGetters() {
        return getters.values();
    }

    private void setBestConstructor(List<Getter> getters){
        constructor = null;
        constructorProperties.clear();
        for(Constructor<?> c : cls.getDeclaredConstructors()){
            if(constructor != null && constructor.getParameterCount() > c.getParameterCount()){
                continue;
            }
            //FieldNames fn = c.getDeclaredAnnotation(FieldNames.class);
            Parameter[] allParams = c.getParameters();
            if(allParams.length > 0 && Modifier.isPublic(c.getModifiers()) == false){
                continue;
            }
            List<Getter> paramProps = new ArrayList<>();
            FieldNames fieldNames = c.getDeclaredAnnotation(FieldNames.class);
            for(int t=0; t<allParams.length;t++){
                Parameter p = allParams[t];
                //String name = fn == null ? p.getName() : fn.names()[t];
                String name = p.getName();
                if(fieldNames != null){
                    name = fieldNames.names()[t];
                }
                /*if(("arg" + t).equals(name)){
                    log.warn("Class " + cls + " constructor with name " + name);
                }*/
                Getter found = null;
                for(Getter pdef : getters){
                    if(pdef.propertyName.equals(name)){
                        if(pdef.getter.getPropertyClass().equals(p.getType())){
                            found = pdef;
                            break;
                        }
                    }
                }

                if(found == null){
                    break;
                }
                paramProps.add(found);
            }
            if(paramProps.size() == allParams.length){
                constructor = c;
                constructorProperties = paramProps;
            }
        }
        if(constructor != null){
            constructor.setAccessible(true);
        }
    }

    public void checkNullFields(C obj){
        for(Getter g : getters.values()){
            if(g.isNullable == false) {
                if(g.getter.get(obj) == null){
                    throw new IllegalStateException("Field " + g.propertyName + " in object of class " + this.cls.getName() + " is null!" );
                }
            }
        }
    }

    public int hashCode(C obj, Lens<C,?>...lenses){
        int result = 1;
        for(Lens<C,?> l : lenses){
            Object value = l.get(obj);
            if(value != null){
                result += 31 * value.hashCode();
            }
        }
        return result;
    }

    public int hashCode(C obj,String...properties) {
        PStream<String> names = (properties == null || properties.length==0 )? getters.keys() :  PStream.from(properties);
        int result = 1;
        for(String prop : names){
            Object value = get(obj,prop);
            if(value != null){
                result += 31 * value.hashCode();
            }
        }
        return result;
    }

    public int hashCodeAll(C obj){
        return hashCode(obj,getters.keys().toArray());
    }


    public String toStringAll(C obj, boolean ignoreNull) {
        String result = cls.getSimpleName() + "[";
        boolean first = true;
        for(String prop : getters.keys()){
            Object value = get(obj,prop);
            if(value == null && ignoreNull){
                continue;
            }
            if(first == false){
                result += ", ";
            } else {
                first = false;
            }
            result += prop + "=" + value;
        }
        return result + "]";
    }

    public String toString(C obj, String n1, Lens<C,?> l1){
        return toString(obj,new Pair<>(n1,l1));
    }
    public String toString(C obj, String n1, Lens<C,?> l1, String n2, Lens<C,?> l2){
        return toString(obj,new Pair<>(n1,l1),new Pair<>(n2,l2));
    }
    public String toString(C obj, String n1, Lens<C,?> l1, String n2, Lens<C,?> l2, String n3, Lens<C,?> l3){
        return toString(obj,new Pair<>(n1,l1),new Pair<>(n2,l2),new Pair<>(n3,l3));
    }
    public String toString(C obj, String n1, Lens<C,?> l1, String n2, Lens<C,?> l2, String n3, Lens<C,?> l3, String n4, Lens<C,?> l4){
        return toString(obj,new Pair<>(n1,l1),new Pair<>(n2,l2),new Pair<>(n3,l3),new Pair<>(n4,l4));
    }
    public String toString(C obj, String n1, Lens<C,?> l1, String n2, Lens<C,?> l2, String n3, Lens<C,?> l3, String n4, Lens<C,?> l4, String n5, Lens<C,?> l5){
        return toString(obj,new Pair<>(n1,l1),new Pair<>(n2,l2),new Pair<>(n3,l3),new Pair<>(n4,l4),new Pair<>(n5,l5));
    }
    public String toString(C obj, Pair<String,Lens<C,?>>... naamLenzen){
        String result = cls.getSimpleName() + "[";
        boolean first = true;
        for(Pair<String,Lens<C,?>> p : naamLenzen){
            if(first){
                first = false;
            } else {
                result += ", ";
            }
            String naam = p.getLeft();
            if(naam != null && naam.isEmpty() == false){
                result += naam + " = ";
            }
            Object v1  = p.getRight().get(obj);
            if(v1 instanceof String){
                v1 = "\"" + v1 + "\"";
            }
            result += "" + v1;
        }

        return result + "]";
    }

    public boolean equals(C left, Object right, Lens<C,?>...lenzen){
        if(right == null || right.getClass().equals(left.getClass())){
            return false;
        }
        for(Lens<C,?>l : lenzen){
            Object v1  = l.get(left);
            Object v2 = l.get((C)right);
            if(v1 == null && v2 != null){
                return false;
            }
            if(v1.equals(v2) == false){
                return false;
            }
        }
        return true;
    }

    public boolean equalsAll(C left,Object right){
        if(right == null || right.getClass().equals(left.getClass()) == false){
            return false;
        }
        for(String prop : getters.keys()){
            Object v1  = get(left,prop);
            Object v2 = get((C)right,prop);
            if(v1 == null){
                return v2 == null;
            }
            if(v1.equals(v2) == false){
                return false;
            }
        }
        return true;
    }

    public boolean equals(C left, Object right, String...properties){
        if(right == null || right.getClass().equals(left.getClass())){
            return false;
        }
        //Collection<String> names = (properties == null || properties.length==0 )? getters.keySet() :  Arrays.asList(properties);
        PStream<String> names = (properties == null || properties.length==0 )? getters.keys() :  PStream.from(properties);
        for(String prop : names){
            Object v1  = get(left,prop);
            Object v2 = get((C)right,prop);
            if(v1 == null && v2 != null){
                return false;
            }
            if(v1.equals(v2) == false){
                return false;
            }
        }
        return true;
    }

    public <X> Lens<C,X> lens(String propertyName){
        if(getters.containsKey(propertyName) == false){
            throw new RuntimeException("Creating lens with name " + propertyName + " on class " + cls.getName() + " failed: property does not exists");
        }
        return new ImLens<>(propertyName);
    }
    static public String firstCharUppercase(String str){
        return "" + Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }


}
