package com.persistentbit.core.config;

import com.persistentbit.core.io.IO;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.UNumber;
import com.persistentbit.core.utils.UReflect;
import com.persistentbit.core.utils.UString;
import com.persistentbit.core.validation.EmailValidator;

import java.net.URI;
import java.net.URL;
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
		return new ConfigVar<>(name,v -> Result.result(v))
				.withInfo("A text strings");
	}
	public static ConfigVar<Integer> intVar(String name){
		return new ConfigVar<>(name, UNumber::parseInt)
				.withInfo("An Integer number");
	}
	public static ConfigVar<Long> longVar(String name){
		return new ConfigVar<>(name, UNumber::parseLong)
				.withInfo("A Long value");
	}
	public static ConfigVar<Double> doubleVar(String name){
		return new ConfigVar<>(name, UNumber::parseDouble)
				.withInfo("A Double value");
	}
	public static ConfigVar<Boolean> boolVar(String name){
		return new ConfigVar<>(name, UString::parseBoolean)
				.withInfo("Boolean value (true or false)");
	}
	public static ConfigVar<Path> pathVar(String name){
		return new ConfigVar<>(name,str -> Result.noExceptions(()-> Paths.get(str)))
				.withInfo("Filesystem path");
	}
	public static ConfigVar<Class> classVar(String name){
		return new ConfigVar<>(name,
				str -> UReflect.getClass(str),
				cls -> Result.result(cls == null ? null : cls.getName())
		).withInfo("Class name");
	}
	public static <E extends Enum> ConfigVar<E> enumValue(String name, Class<E> enumCls){
		return new ConfigVar<E>(name,
				str -> {
					E instance = UReflect.getEnumInstances(enumCls).find(e -> e.name().equals(str)).orElse(null);
					if(instance == null){
						return Result.failure("No enum value with name '" + str + "' found for enum " + UReflect.typeToSimpleString(enumCls));
					}
					return Result.success(instance);
				},
				value -> Result.result(value == null ? null : value.name() )
		)
				.withInfo("Enum '" + UReflect.typeToSimpleString(enumCls) + "'. One of " + UReflect.getEnumInstances(enumCls).map(e -> e.name()).toString(", "));

	}

	public static ConfigVar<String> emailVar(String name){
		return stringVar(name)
				.withValidator(EmailValidator.stringValidator.toValidator())
				.withInfo("An e-mail address");
	}

	public static ConfigVar<URL> urlVar(String name){
		return new ConfigVar<>(
				name,
				str -> IO.asURL(str),
				url -> Result.result(url == null ? null : url.toExternalForm())
		).withInfo("An URL");
	}
	public static ConfigVar<URI> uriVar(String name){
		return new ConfigVar<>(
				name,
				str -> IO.asURI(str),
				uri -> Result.result(uri == null ? null : uri.toString())
		).withInfo("An URI");
	}
}
