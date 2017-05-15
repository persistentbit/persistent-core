package com.persistentbit.core.experiments.config;

import com.persistentbit.core.utils.UString;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/05/2017
 */
public class ConfigBool extends MemConfigVar<Boolean,ConfigBool> {
    public ConfigBool(String name){
        super(name, UString::parseBoolean);
    }
}
