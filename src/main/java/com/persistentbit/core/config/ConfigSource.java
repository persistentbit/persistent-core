package com.persistentbit.core.config;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.doc.annotations.DUsesClass;
import com.persistentbit.core.result.Result;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 11/05/2017
 */
@DUsesClass(Config.class)
public interface ConfigSource {
    <T> Config<T> add(String name, Class<T> type, T defaultValue, String info);
    <T> Config<PList<T>> addArray(String name, Class<T> cls, PList<T> defaultValue, String info);
    <T> Result<Config<T>> get(String name);

    default Config<Integer> addInt(String name, Integer defaultValue, String info){
        return add(name,Integer.class, defaultValue, info);
    }
    default Config<Long> addLong(String name, Long defaultValue, String info){
        return add(name,Long.class, defaultValue, info);
    }
    default Config<String> addString(String name, String defaultValue, String info){
        return add(name,String.class, defaultValue, info);
    }
    default Config<Boolean> addBoolean(String name, Boolean defaultValue, String info){
        return add(name,Boolean.class, defaultValue, info);
    }
    default <E extends Enum> Config<E> addEnum(String name,Class<E> cls,  E defaultValue, String info){
        return add(name,cls, defaultValue, info);
    }

    default Config<PList<Integer>> addIntArray(String name, PList<Integer> defaultValue, String info){
        return addArray(name,Integer.class, defaultValue, info);
    }
    default Config<PList<Long>> addLongArray(String name, PList<Long> defaultValue, String info){
        return addArray(name,Long.class, defaultValue, info);
    }
    default Config<PList<String>> addStringArray(String name, PList<String> defaultValue, String info){
        return addArray(name,String.class, defaultValue, info);
    }
    default Config<PList<Boolean>> addBooleanArray(String name, PList<Boolean> defaultValue, String info){
        return addArray(name,Boolean.class, defaultValue, info);
    }
    default <E extends Enum> Config<PList<E>> addEnumArray(String name,Class<E> cls,  PList<E> defaultValue, String info){
        return addArray(name,cls, defaultValue, info);
    }

    default ConfigSource subGroup(String name){
        return new SubConfigSource(this,name);
    }
}
