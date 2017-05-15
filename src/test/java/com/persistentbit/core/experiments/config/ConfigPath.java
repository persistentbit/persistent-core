package com.persistentbit.core.experiments.config;

import com.persistentbit.core.result.Result;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/05/2017
 */
public class ConfigPath extends MemConfigVar<Path,ConfigPath>{
    public ConfigPath(String name){
        super(name,str -> Result.noExceptions(()-> Paths.get(str)));
    }

}
