package com.persistentbit.core.utils;

import com.persistentbit.core.collections.PMap;

import java.lang.reflect.*;
import java.util.Objects;
import java.util.Optional;

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
}
