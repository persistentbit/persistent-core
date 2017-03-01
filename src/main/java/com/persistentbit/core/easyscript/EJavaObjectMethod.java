package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.IPMap;
import com.persistentbit.core.collections.IPSet;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.NumberUtils;
import com.persistentbit.core.utils.ReflectionUtils;
import com.persistentbit.core.utils.StrPos;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;


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
	private Class[] prevKey;
	private Method prevMethod;

	/*static private class CacheKey{
		public final Class prevValueCls;
		public final Class[] prevArgCls;

		public CacheKey(Class prevValueCls, Class[] prevArgCls) {
			this.prevValueCls = prevValueCls;
			this.prevArgCls = prevArgCls;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(!(o instanceof CacheKey)) return false;

			CacheKey cacheKey = (CacheKey) o;

			if(!prevValueCls.equals(cacheKey.prevValueCls)) return false;
			// Probably incorrect - comparing Object[] arrays with Arrays.equals
			return Arrays.equals(prevArgCls, cacheKey.prevArgCls);
		}

		@Override
		public int hashCode() {
			int result = prevValueCls.hashCode();
			result = 31 * result + Arrays.hashCode(prevArgCls);
			return result;
		}
	}*/

	public EJavaObjectMethod(StrPos pos, Object value,
							 PList<Method> methods
	) {
		this.pos = pos;
		this.value = value;
		this.methods = methods;
	}

	@Override
	public Object apply(Object... arguments) {

		Class[] argClasses = new Class[arguments.length];
		for(int t = 0; t < arguments.length; t++) {
			argClasses[t] = arguments[t] == null ? null : arguments[t].getClass();
		}
		if(prevKey != null && prevMethod != null && Arrays.equals(argClasses, prevKey) == false) {
			System.out.println("Cached: " + prevMethod);
			Method                       m           = prevMethod;
			Class[]                      paramTypes  = m.getParameterTypes();
			Tuple2<MatchLevel, Object[]> matchResult = tryMatch(paramTypes, arguments);
			checkMatch(pos, matchResult, arguments, paramTypes);
			return invoke(m, matchResult._2);
		}
		prevKey = argClasses;
		prevMethod = null;

		if(arguments.length == 0) {
			for(Method m : methods) {
				if(m.getParameterCount() == arguments.length) {

					return invoke(m);
				}
			}
		}
		if(methods.size() == 1) {
			Method m = methods.head();
			Class[] paramTypes = m.getParameterTypes();
			Tuple2<MatchLevel, Object[]> matchResult = tryMatch(paramTypes, arguments);
			checkMatch(pos,matchResult,arguments,paramTypes);
			return invoke(m, matchResult._2);
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
			Class[] paramTypes = first.getParameterTypes();
			Tuple2<MatchLevel, Object[]> matchResult = tryMatch(paramTypes, arguments);
			checkMatch(pos,matchResult,arguments,paramTypes);
			return invoke(first, matchResult._2);
		}
		otherPossibles = otherPossibles.plus(first);

		//otherPosibles is now all methods with the same number of arguments
		//So we have to select the correct one for the argument types;


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
			prevMethod = m;
			return m.invoke(value, arguments);
		} catch(IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
			String on = value == null ? m.getDeclaringClass().getName() : value.toString();
			String args = PStream.from(arguments).toString(", ");
			String call = on + "." + m.getName() + "(" + args + ")";
			throw new EvalException(pos, "Error while invoking " + call, e);
		}
	}

	private static Object invoke(StrPos pos,Constructor c, Object... arguments) {
		try {
			return c.newInstance(arguments);
		} catch(IllegalAccessException | InstantiationException | InvocationTargetException e) {
			throw new EvalException(pos, "Error instantiating '" + c.getName(), e);
		}
	}

	private enum MatchLevel{
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

		if(cls.getAnnotation(FunctionalInterface.class) != null && value instanceof ERuntimeLambda){
			Optional<Method> optMethod = ReflectionUtils.getFunctionalInterfaceMethod(cls);
			if(optMethod.isPresent()){
				ERuntimeLambda lambda = (ERuntimeLambda)value;
				Function<Object[],Object> impl = lambda::apply;
				return Optional.of(ReflectionUtils.createProxyForFunctionalInterface(cls,impl));
			}

		}


		return Optional.empty();
	}

	public static Object construct(StrPos pos, Class cls,Object... arguments) {
		Constructor[] constructors = cls.getConstructors();
		if(arguments.length == 0) {
			for(Constructor m : constructors) {
				if(m.getParameterCount() == arguments.length) {

					return invoke(pos,m);
				}
			}
		}
		if(constructors.length == 1) {
			Class[] paramTypes = constructors[0].getParameterTypes();
			Tuple2<MatchLevel, Object[]> matchResult = tryMatch(paramTypes, arguments);
			checkMatch(pos,matchResult,arguments,paramTypes);
			return invoke(pos,constructors[0], matchResult._2);
		}
		Constructor        first          = null;
		PList<Constructor> otherPossibles = PList.empty();
		for(Constructor m : constructors) {
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
			throw new EvalException(pos, "Can't find construct for class  '" + cls.getName()+ "' with " + arguments.length + " parameters");
		}
		if(otherPossibles.isEmpty()) {
			//We have only 1 method with the same number of arguments
			Class[] paramTypes = first.getParameterTypes();
			Tuple2<MatchLevel, Object[]> matchResult = tryMatch(paramTypes, arguments);
			checkMatch(pos,matchResult,arguments,paramTypes);
			return invoke(pos,first, matchResult._2);
		}
		otherPossibles = otherPossibles.plus(first);

		//otherPosibles is now all methods with the same number of arguments
		//So we have to select the correct one for the argument types;

		Class[] argClasses = new Class[arguments.length];
		for(int t = 0; t < arguments.length; t++) {
			argClasses[t] = arguments[t] == null ? null : arguments[t].getClass();
		}
		Constructor   partial            = null;
		Object[] partialArgs        = null;
		boolean  hasMultiplePartial = false;

		for(Constructor m : otherPossibles) {
			Tuple2<MatchLevel, Object[]> matchResult = tryMatch(m.getParameterTypes(), arguments);
			if(matchResult._1 == MatchLevel.full) {
				return invoke(pos,m, matchResult._2);
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
			return invoke(pos,partial, partialArgs);
		}
		throw new EvalException(pos, "Method arguments don't match for java method application!");
	}

	private static void checkMatch(StrPos pos,Tuple2<MatchLevel,Object[]> mr, Object[] arguments,Class[] types){
		if(mr._1 == MatchLevel.not){
			String args = PStream.from(arguments).toString(", ");
			String params = PStream.from(types).map(v -> v.getName()).toString(", ");
			throw new EvalException(pos,"Can't match parameters (" + args + ") with (" + params + ")");
		}
	}
}
