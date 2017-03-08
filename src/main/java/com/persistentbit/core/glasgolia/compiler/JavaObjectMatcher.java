package com.persistentbit.core.glasgolia.compiler;

import com.persistentbit.core.collections.IPMap;
import com.persistentbit.core.collections.IPSet;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.glasgolia.compiler.rexpr.RFunction;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.UNumber;
import com.persistentbit.core.utils.UReflect;

import java.lang.reflect.Array;
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

	public static Tuple2<MatchLevel, Object[]> tryMatch(Class[] paramTypes, Object[] arguments,boolean withVarArgs) {
		if(withVarArgs == false){
			if (paramTypes.length != arguments.length) {
				return Tuple2.of(MatchLevel.not, new Object[0]);
			}
			return tryMatch(paramTypes,arguments,paramTypes.length);

		}
		//Try match with varargs...

		if(paramTypes.length> arguments.length){
			return Tuple2.of(MatchLevel.not, new Object[0]);
		}
		Tuple2<JavaObjectMatcher.MatchLevel, Object[]> matchResult = tryMatch(paramTypes, arguments,paramTypes.length-1);
		if(matchResult._1 == JavaObjectMatcher.MatchLevel.not){
			return Tuple2.of(MatchLevel.not, new Object[0]);
		}
		Class itemClass = paramTypes[paramTypes.length-1].getComponentType();
		int restLength = arguments.length-(paramTypes.length-1);
		Object[] varArgs = (Object[]) Array.newInstance(itemClass, restLength);
		boolean ok= true;
		for(int t=0; t<varArgs.length; t++){
			Object value = arguments[paramTypes.length-1+t];
			Optional<Object> casted = JavaObjectMatcher.tryCast(value,itemClass);
			if(casted.isPresent() == false){
				return Tuple2.of(MatchLevel.not, new Object[0]);
			}
			varArgs[t] = casted.get();
		}
		Object[] newSet = new Object[paramTypes.length];
		System.arraycopy(matchResult._2, 0, newSet, 0, paramTypes.length - 1);
		newSet[paramTypes.length - 1] = varArgs;
		return Tuple2.of(matchResult._1,newSet);
	}
	public static Tuple2<MatchLevel, Object[]> tryMatch(Class[] paramTypes, Object[] arguments,int compareCount) {
		MatchLevel level = MatchLevel.full;
		Object[] converted = new Object[compareCount];
		for(int t = 0; t < compareCount; t++) {
			Optional<Object> convertedArg = tryCast(arguments[t],paramTypes[t]);
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
		cls = UReflect.convertPrimitiveClassToObjectClass(cls).orElse(cls);
		if(cls.isAssignableFrom(valueCls)) {
			return Optional.of((R) value);
		}
		if(value instanceof Number && UNumber.isNumberClass(cls)) {
			return UNumber.convertTo((Number) value, cls)
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
		if(cls.getAnnotation(FunctionalInterface.class) != null){
			if(value instanceof RFunction){
				Optional<Method> optMethod = UReflect.getFunctionalInterfaceMethod(cls);
				if(optMethod.isPresent()) {
					Function<Object[], Object> impl   = ((RFunction) value)::apply;
					return Optional.of(UReflect.createProxyForFunctionalInterface(cls, impl));
				}
			}
		}

		return Optional.empty();
	}

}
