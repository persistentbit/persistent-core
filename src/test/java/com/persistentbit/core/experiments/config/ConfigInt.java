package com.persistentbit.core.experiments.config;

import com.persistentbit.core.utils.UNumber;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/05/2017
 */
public class ConfigInt extends MemConfigVar<Integer,ConfigInt>{
    public ConfigInt(String name) {
        super(name, UNumber::parseInt);
    }
}
