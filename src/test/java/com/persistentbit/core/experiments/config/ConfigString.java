package com.persistentbit.core.experiments.config;

import com.persistentbit.core.result.Result;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/05/2017
 */
public class ConfigString extends MemConfigVar<String,ConfigString>{
    public ConfigString(String name) {
        super(name, v -> Result.result(v));
    }
}
