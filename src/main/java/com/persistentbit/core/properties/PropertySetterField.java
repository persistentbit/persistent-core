package com.persistentbit.core.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Set the property on an Object using a Reflection {@link Field} instance.<br>
 *
 * @see PropertySetter
 * @see PropertySetterMethod
 */
public class PropertySetterField implements PropertySetter{

  private final Field field;

  /**
   * @param field The {@link Field} instance to use for setting a property
   */
  public PropertySetterField(Field field) {
	this.field = field;
	field.setAccessible(true);
  }

  @Override
  public void set(Object container, Object value) {
	try {
	  field.set(container, value);
	} catch(Exception e) {
	  throw new RuntimeException(e);
	}
  }

  @Override
  public Type getPropertyType() {
	return field.getGenericType();
  }

  @Override
  public Class<?> getPropertyClass() {
	return field.getType();
  }
}
