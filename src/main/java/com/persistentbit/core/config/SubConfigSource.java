package com.persistentbit.core.config;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.result.Result;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 12/05/2017
 */
public class SubConfigSource implements ConfigSource{
    private final ConfigSource master;
    private final String subName;

    public SubConfigSource(ConfigSource master, String subName) {
        this.master = master;
        this.subName = subName;
    }

    @Override
    public <T> Config<T> add(String name, Class<T> type, T defaultValue, String info) {
        return master.add(subName + "." + name, type,defaultValue,info);
    }

    @Override
    public <T> Config<PList<T>> addArray(String name, Class<T> cls, PList<T> defaultValue, String info) {
        return master.addArray(subName + "." + name, cls,defaultValue,info);
    }

    @Override
    public <T> Result<Config<T>> get(String name) {
        return master.get(subName + "." + name);
    }
}
