package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.IPMap;
import com.persistentbit.core.collections.IPSet;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.NumberUtils;
import com.persistentbit.core.utils.ReflectionUtils;
import com.persistentbit.core.utils.StrPos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * TODOC
 *
 * @author petermuys
 * @since 25/02/17
 */
public class EJavaObjectMethod implements ECallable{

	private final StrPos pos;
	private final Object value;
	private final PList<Method> methods;

	public EJavaObjectMethod(StrPos pos, Object value,
							 PList<Method> methods
	) {
		this.pos = pos;
		this.value = value;
		this.methods = methods;
	}

	@Override
	public Object apply(Object... arguments) {
		if(arguments.length == 0) {
			for(Method m : methods) {
				if(m.getParameterCount() == arguments.length) {
					return invoke(m, arguments);
				}
			}
		}
		if(methods.size() == 1) {
			return invoke(methods.head(), arguments);
		}
		Method        first          = null;
		PList<Method> otherPossibles = PList.empty();
		for(Method m : methods) {
			if(m.getParameterCount() == arguments.length) {
				if(first == null) {
					first = m;
				}
				else {
					otherPossibles = otherPossibles.plus(m);
				}
			}
		}
		if(first == null) {
			throw new EvalException(pos, "Can't find method '" + methods.head()
																		.getName() + "' with " + arguments.length + " parameters");
		}
		if(otherPossibles.isEmpty()) {
			//We have only 1 method with the same number of arguments
			return invoke(first, arguments);
		}
		otherPossibles = otherPossibles.plus(first);

		//otherPosibles is now all methods with the same number of arguments
		//So we have to select the correct one for the argument types;

		Class[] argClasses = new Class[arguments.length];
		for(int t = 0; t < arguments.length; t++) {
			argClasses[t] = arguments[t] == null ? null : arguments[t].getClass();
		}
		Method   partial            = null;
		Object[] partialArgs        = null;
		boolean  hasMultiplePartial = false;

		for(Method m : otherPossibles) {
			Tuple2<MatchLevel, Object[]> matchResult = tryMatch(m.getParameterTypes(), arguments);
			if(matchResult._1 == MatchLevel.full) {
				return invoke(m, matchResult._2);
			}
			if(matchResult._1 == MatchLevel.partial) {
				if(partial != null) {
					hasMultiplePartial = true;
				}
				else {
					partial = m;
					partialArgs = matchResult._2;
				}
			}

		}
		if(hasMultiplePartial) {
			throw new EvalException(pos, "Multiple overloaded method for java method application!");
		}
		if(partial != null) {
			return invoke(partial, partialArgs);
		}
		throw new EvalException(pos, "Method arguments don't match for java method application!");
	}

	private Object invoke(Method m, Object... arguments) {
		try {
			return m.invoke(value, arguments);
		} catch(IllegalAccessException | InvocationTargetException e) {
			throw new EvalException(pos, "Error while invoking java method '" + m.getName() + "()' on " + value, e);
		}
	}

	private enum MatchLevel{
		full, partial, not
	}

	static Tuple2<MatchLevel, Object[]> tryMatch(Class[] paramTypes, Object[] arguments) {
		MatchLevel level     = MatchLevel.full;
		Object[]   converted = new Object[paramTypes.length];
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

	static <R> Optional<R> tryCast(Object value, Class<R> cls) {
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
		return Optional.empty();
	}
}
