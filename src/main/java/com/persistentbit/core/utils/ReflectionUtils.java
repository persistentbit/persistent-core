package com.persistentbit.core.utils;

import java.lang.reflect.*;

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
}
