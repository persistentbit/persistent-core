package com.persistentbit.core.glasgolia.compiler;

import com.persistentbit.core.collections.IPMap;
import com.persistentbit.core.collections.IPSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.easyscript.ERuntimeLambda;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.NumberUtils;
import com.persistentbit.core.utils.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class JavaObjectMatcher{

	public enum MatchLevel{
		full, partial, not
	}

	public static Tuple2<MatchLevel, Object[]> tryMatch(Class[] paramTypes, Object[] arguments) {
		MatchLevel level     = MatchLevel.full;
		Object[]   converted = new Object[paramTypes.length];
		if(paramTypes.length != arguments.length) {
			return Tuple2.of(MatchLevel.not, converted);
		}
		for(int t = 0; t < paramTypes.length; t++) {
			Optional<Object> convertedArg = tryCast(arguments[t], paramTypes[t]);
			if(convertedArg.isPresent() == false) {
				return Tuple2.of(MatchLevel.not, null);
			}
			converted[t] = convertedArg.get();
			if(converted[t] != arguments[t]) {
				level = MatchLevel.partial;
			}
		}
		return Tuple2.of(level, converted);
	}

	public static <R> Optional<R> tryCast(Object value, Class<R> cls) {
		if(value == null) {
			return Optional.of(null);
		}
		Class valueCls = value.getClass();
		if(cls.isAssignableFrom(valueCls)) {
			return Optional.of((R) value);
		}
		cls = ReflectionUtils.convertPrimitiveClassToObjectClass(cls).orElse(cls);
		if(cls.isAssignableFrom(valueCls)) {
			return Optional.of((R) value);
		}
		if(value instanceof Number && NumberUtils.isNumberClass(cls)) {
			return NumberUtils.convertTo((Number) value, cls)
							  .getOpt();
		}
		if(cls.isAssignableFrom(List.class)) {
			if(value instanceof PStream) {
				return Optional.of((R) ((PStream) value).list());
			}
		}
		if(cls.isAssignableFrom(Map.class)) {
			if(value instanceof IPMap) {
				return Optional.of((R) ((IPMap) value).map());
			}
		}
		if(cls.isAssignableFrom(Set.class)) {
			if(value instanceof IPSet) {
				return Optional.of((R) ((IPSet) value).pset().toSet());
			}
		}

		if(cls.getAnnotation(FunctionalInterface.class) != null && value instanceof ERuntimeLambda) {
			Optional<Method> optMethod = ReflectionUtils.getFunctionalInterfaceMethod(cls);
			if(optMethod.isPresent()) {
				ERuntimeLambda             lambda = (ERuntimeLambda) value;
				Function<Object[], Object> impl   = lambda::apply;
				return Optional.of(ReflectionUtils.createProxyForFunctionalInterface(cls, impl));
			}

		}


		return Optional.empty();
	}

}
