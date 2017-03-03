package com.persistentbit.core.utils;

import com.persistentbit.core.collections.PMap;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Helper Utilities for using Reflection.
 *
 * @author Peter Muys
 * @since 13/07/2016
 */
public final class ReflectionUtils{

  public static Class<?> classFromType(Type t) {
	if(t instanceof Class) {
	  return (Class<?>) t;
	}
	if(t instanceof ParameterizedType) {
	  return classFromType(((ParameterizedType) t).getRawType());
	}
	if(t instanceof GenericArrayType) {
	  GenericArrayType gat = (GenericArrayType) t;
	  throw new RuntimeException(gat.getTypeName());
	}
	if(t instanceof WildcardType) {
	  WildcardType wct = (WildcardType) t;
	  return classFromType(wct.getUpperBounds()[0]);
	}
	if(t instanceof TypeVariable) {
	  return Object.class;
	}
	throw new RuntimeException("Don't know how to handle " + t);
  }

	private static final PMap<Class, Class> primitiveClassToObjectClassLookup;

	static {
		primitiveClassToObjectClassLookup = PMap.<Class, Class>empty()
			.put(boolean.class, Boolean.class)
			.put(char.class, Character.class)
			.put(byte.class, Byte.class)
			.put(short.class, Short.class)
			.put(int.class, Integer.class)
			.put(long.class, Long.class)
			.put(float.class, Float.class)
			.put(double.class, Double.class)
		;
	}

	public static Optional<Class> convertPrimitiveClassToObjectClass(Class cls) {
		return primitiveClassToObjectClassLookup.getOpt(cls);
	}

	public static boolean isPrimitiveClass(Class cls) {
		return convertPrimitiveClassToObjectClass(cls).isPresent();
	}

	public static Optional<Class> getClass(String name) {
		try {
			return Optional.of(Class.forName(Objects.requireNonNull(name)));
		} catch(ClassNotFoundException e) {
			return Optional.empty();
		}
	}

	public static Optional<Method> getGetter(Class cls,String name){
		name = StringUtils.firstUpperCase(name);
		try {
			return Optional.of(cls.getMethod("get" + name));
		} catch(NoSuchMethodException e) {
			try {
				return Optional.of(cls.getMethod("is" + name));
			} catch(NoSuchMethodException e2) {
				return Optional.empty();
			}
		}

	}

	public static Optional<Field> getField(Class cls, String name) {
		try {
			return Optional.of(cls.getField(name));
		} catch(NoSuchFieldException e) {
			return Optional.empty();
		}
	}

	public static Optional<Method> getFunctionalInterfaceMethod(Class functionalInterfaceClass){
		for(Method m : functionalInterfaceClass.getMethods()){
			if(m.isDefault() == false){
				return Optional.of(m);
			}
		}
		return Optional.empty();
	}

	public static <T> T createProxyForFunctionalInterface(Class<T> clsFunctionalInterface, Function<Object[],Object> implementation){
		InvocationHandler handler=  (proxy, method, args) -> {
			if (method.isDefault())
			{
				final Class<?> declaringClass = method.getDeclaringClass();
				return
					MethodHandles.lookup()
						.in(declaringClass)
						.unreflectSpecial(method, declaringClass)
						.bindTo(proxy)
						.invokeWithArguments(args);
			}
			return implementation.apply(args);
		};
		Class[] clsList = new Class[]{ clsFunctionalInterface};
		return (T)Proxy.newProxyInstance(clsFunctionalInterface.getClassLoader(),clsList,handler);
	}

	public static final InvocationHandler invocationHandlerWithDefaults = (proxy, method, args) -> {
		if (method.isDefault())
		{
			final Class<?> declaringClass = method.getDeclaringClass();
			return
				MethodHandles.lookup()
					.in(declaringClass)
					.unreflectSpecial(method, declaringClass)
					.bindTo(proxy)
					.invokeWithArguments(args);
		}

		// proxy impl of not defaults methods
		return null;
	};
}
