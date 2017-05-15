package com.persistentbit.core.experiments.config;

import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.UNumber;
import com.persistentbit.core.utils.UString;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODOC
 *
 * @author petermuys
 * @since 15/05/17
 */
public class Config{

	public static ConfigVar<String> stringVar(String name){
		return new ConfigVar<>(name,v -> Result.result(v));
	}
	public static ConfigVar<Integer> intVar(String name){
		return new ConfigVar<>(name, UNumber::parseInt);
	}
	public static ConfigVar<Long> longVar(String name){
		return new ConfigVar<>(name, UNumber::parseLong);
	}
	public static ConfigVar<Double> doubleVar(String name){
		return new ConfigVar<>(name, UNumber::parseDouble);
	}
	public static ConfigVar<Boolean> boolVar(String name){
		return new ConfigVar<>(name, UString::parseBoolean);
	}
	public static ConfigVar<Path> pathVar(String name){
		return new ConfigVar<>(name,str -> Result.noExceptions(()-> Paths.get(str)));
	}

}
