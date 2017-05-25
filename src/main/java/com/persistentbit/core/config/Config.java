package com.persistentbit.core.config;

import com.persistentbit.core.doc.annotations.DCreates;
import com.persistentbit.core.doc.annotations.DSupport;
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
 * Utility class to create different kind of {@link ConfigVar} instances.<br>
 * Typically used by statically importing this Class.<br>
 *
 * @author petermuys
 * @since 15/05/17
 */
@DSupport
@DCreates(value = ConfigVar.class)
public class Config{

	/**
	 * Create a config var containing a String
	 * @param name The name of the var
	 * @return a String config var
	 */
	public static ConfigVar<String> stringVar(String name){
		return new ConfigVar<>(name,v -> Result.result(v))
				.withInfo("A text strings");
	}

	/**
	 * Create a config var containing an Integer
	 * @param name The name of the var
	 * @return an Integer config var
	 */
	public static ConfigVar<Integer> intVar(String name){
		return new ConfigVar<>(name, UNumber::parseInt)
				.withInfo("An Integer number");
	}

	/**
	 * Create a config var containing a Long value
	 * @param name The name of the var
	 * @return a Long config var
	 */
	public static ConfigVar<Long> longVar(String name){
		return new ConfigVar<>(name, UNumber::parseLong)
				.withInfo("A Long value");
	}

	/**
	 * Create a config var containing a Double value
	 * @param name The name of the var
	 * @return a Double config var
	 */
	public static ConfigVar<Double> doubleVar(String name){
		return new ConfigVar<>(name, UNumber::parseDouble)
				.withInfo("A Double value");
	}

	/**
	 * Create a config var containing a Boolean value
	 * @param name The name of the var
	 * @return a Boolean config var
	 */
	public static ConfigVar<Boolean> boolVar(String name){
		return new ConfigVar<>(name, UString::parseBoolean)
				.withInfo("Boolean value (true or false)");
	}

	/**
	 * Create a config var containing a {@link Path} value
	 * @param name The name of the var
	 * @return a Path config var
	 */
	public static ConfigVar<Path> pathVar(String name){
		return new ConfigVar<>(name,str -> Result.noExceptions(()-> Paths.get(str)))
				.withInfo("Filesystem path");
	}

	/**
	 * Create a config var containing a {@link Class} value
	 * @param name The name of the var
	 * @return a Class config var
	 */
	public static ConfigVar<Class> classVar(String name){
		return new ConfigVar<>(name,
				str -> UReflect.getClass(str),
				cls -> Result.result(cls == null ? null : cls.getName())
		).withInfo("Class name");
	}

	/**
	 * Create a config var containing an Enum value
	 * @param name The name of the var
	 * @param enumCls The enum class
	 * @param <E> The type of the enum
	 * @return a Enum config var
	 */
	public static <E extends Enum> ConfigVar<E> enumValue(String name, Class<E> enumCls){
		return new ConfigVar<E>(name,
				str -> {
					E instance = UReflect.getEnumInstances(enumCls).find(e -> e.name().equals(str)).orElse(null);
					if(instance == null){
						return Result.failure(name + ": " + "No enum value with name '" + str + "' found for enum " + UReflect.typeToSimpleString(enumCls));
					}
					return Result.success(instance);
				},
				value -> Result.result(value == null ? null : value.name() )
		)
				.withInfo("Enum '" + UReflect.typeToSimpleString(enumCls) + "'. One of " + UReflect.getEnumInstances(enumCls).map(e -> e.name()).toString(", "));

	}

	/**
	 * Create a config var containing a valid e-mail address
	 * @param name The name of the var
	 * @return a e-mail address config var
	 */
	public static ConfigVar<String> emailVar(String name){
		return stringVar(name)
				.withValidator(EmailValidator.stringValidator.toValidator())
				.withInfo("An e-mail address");
	}

	/**
	 * Create a config var containing an {@link URL}
	 * @param name The name of the var
	 * @return an URL config var
	 */
	public static ConfigVar<URL> urlVar(String name){
		return new ConfigVar<>(
				name,
				IO::asURL,
				url -> Result.result(url == null ? null : url.toExternalForm())
		).withInfo("An URL");
	}

	/**
	 * Create a config var containing an {@link URI}
	 * @param name The name of the var
	 * @return an URI config var
	 */
	public static ConfigVar<URI> uriVar(String name){
		return new ConfigVar<>(
				name,
				IO::asURI,
				uri -> Result.result(uri == null ? null : uri.toString())
		).withInfo("An URI");
	}
}
