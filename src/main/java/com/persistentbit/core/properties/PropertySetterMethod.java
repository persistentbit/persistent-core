package com.persistentbit.core.properties;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Set the property on an Object using a Reflection {@link Method} instance.<br>
 *
 * @see PropertySetter
 * @see PropertySetterField
 */
public class PropertySetterMethod implements PropertySetter{

  private final Method method;

  /**
   * @param method The method to use for setting the property on an object
   */
  public PropertySetterMethod(Method method) {
	this.method = method;
	method.setAccessible(true);
  }

  @Override
  public void set(Object container, Object value) {
	try {
	  method.invoke(container, value);
	} catch(Exception e) {
	  throw new RuntimeException(e);
	}
  }

  @Override
  public Type getPropertyType() {
	return method.getGenericParameterTypes()[0];
  }

  @Override
  public Class<?> getPropertyClass() {
	return method.getParameterTypes()[0];
  }
}
