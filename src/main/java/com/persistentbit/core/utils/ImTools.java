package com.persistentbit.core.utils;


import com.persistentbit.core.NotNullable;
import com.persistentbit.core.Nullable;
import com.persistentbit.core.Pair;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.lenses.Lens;
import com.persistentbit.core.properties.FieldNames;
import com.persistentbit.core.properties.PropertyGetter;
import com.persistentbit.core.properties.PropertyGetterField;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;


/**
 * This is a Immutable Objects utility class.<br>
 * This utility class uses reflection to provide multiple utility functions for Immutable Objects.<br>
 * Use {@link ImTools#get(Class)} to get an ImTools instance for a class.<br>
 *
 * @author Peter Muys
 * @since 3/03/2016
 */
public final class ImTools<C>{

  private static final Logger                 log         = Logger.getLogger(ImTools.class.getName());
  private static final Map<Class<?>, ImTools> sClassTools = new HashMap<>();
  private final Class<C> cls;
  private PMap<String, Getter> getters               = PMap.empty();
  private List<Getter>         constructorProperties = new ArrayList<>();
  private Constructor<?> constructor;
  private C              unitObject;

  private ImTools(Class<C> cls) {

	this.cls = cls;
	List<Getter> allGetters = getFields();
	setBestConstructor(allGetters);
	if(constructor == null) {
	  throw new RuntimeException("Can't find constructor for " + this.cls.getName() + " with properties" + allGetters);
	}
	for(Getter g : allGetters) {
	  getters = getters.put(g.propertyName, g);
	}
  }

  private List<Getter> getFields() {
	try {
	  Class<?>     cls    = this.cls;
	  List<Getter> result = new ArrayList<>();
	  while(cls != null && cls.equals(Object.class) == false) {
		for(Field f : cls.getDeclaredFields()) {
		  if(Modifier.isTransient(f.getModifiers())) {
			continue;
		  }
		  if(Modifier.isStatic(f.getModifiers())) {
			continue;
		  }
		  f.setAccessible(true);
		  Boolean isNullable = null;
		  if(f.getAnnotation(Nullable.class) != null) { isNullable = true; }
		  if(f.getAnnotation(NotNullable.class) != null) { isNullable = false; }
		  if(isNullable == null) {
			isNullable = checkGetterNullable(f);
		  }
		  result.add(new Getter(new PropertyGetterField(f), f.getName(), isNullable, f));
		}
		cls = cls.getSuperclass();
	  }
	  return result;
	} catch(Exception e) {
	  throw new RuntimeException(e);
	}
  }

  private void setBestConstructor(List<Getter> getters) {
	constructor = null;
	constructorProperties.clear();
	for(Constructor<?> c : cls.getDeclaredConstructors()) {
	  if(constructor != null && constructor.getParameterCount() > c.getParameterCount()) {
		continue;
	  }
	  //FieldNames fn = c.getDeclaredAnnotation(FieldNames.class);
	  Parameter[] allParams = c.getParameters();
	  if(allParams.length > 0 && Modifier.isPublic(c.getModifiers()) == false) {
		continue;
	  }
	  List<Getter> paramProps = new ArrayList<>();
	  FieldNames   fieldNames = c.getDeclaredAnnotation(FieldNames.class);
	  for(int t = 0; t < allParams.length; t++) {
		Parameter p = allParams[t];
		//String name = fn == null ? p.getName() : fn.names()[t];
		String name = p.getName();
		if(fieldNames != null) {
		  name = fieldNames.names()[t];
		}
				  /*if(("arg" + t).equals(name)){
					log.warn("Class " + cls + " constructor with name " + name);
                }*/
		Getter found = null;
		for(Getter def : getters) {
		  if(def.propertyName.equals(name)) {
			if(def.getter.getPropertyClass().equals(p.getType())) {
			  found = def;
			  break;
			}
		  }
		}

		if(found == null) {
		  break;
		}
		paramProps.add(found);
	  }
	  if(paramProps.size() == allParams.length) {
		constructor = c;
		constructorProperties = paramProps;
	  }
	}
	if(constructor != null) {
	  constructor.setAccessible(true);
	}
  }

  private boolean checkGetterNullable(Field f) {
	Class<?> cls  = this.cls;
	String   get1 = "get" + firstCharUppercase(f.getName());
	String   get2 = "is" + firstCharUppercase(f.getName());


	while(cls != null && cls.equals(Object.class) == false) {
	  for(Method m : cls.getDeclaredMethods()) {
		String name = m.getName();
		if(!name.equals(get1) && !name.equals(get2)) {
		  continue;
		}
		return Optional.class.equals(m.getReturnType());
	  }
	  cls = cls.getSuperclass();
	}
	return false;   //if not defined: assume not nullable
  }

  public static String firstCharUppercase(String str) {
	return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }

  /**
   * Get an existing or create a new ImTools object for the provided class.<br>
   * The class should be of an immutable object<br>
   *
   * @param cls The class of the immutable object
   * @param <C> Type of the class for ImTools instance
   *
   * @return The ImTools instance for the provided class
   */
  @SuppressWarnings("unchecked")
  public static synchronized <C> ImTools<C> get(Class<C> cls) {
	ImTools<C> im = (ImTools<C>) sClassTools.get(cls);
	if(im == null) {
	  im = new ImTools<>(cls);
	  sClassTools.put(cls, im);
	}
	return im;
  }

  /**
   * Get the Class where this ImTools is for
   *
   * @return The Class
   */
  public Class<C> getObjectClass() {
	return cls;
  }

  /**
   * Get or create a unit object for the class C<br>
   * The unit object is created by the default constructor of class C<br>
   * Unit objects are cached and shared between invocations.<br>
   *
   * @return The unit object
   */
  @SuppressWarnings("unchecked")
  public C unit() {
	if(unitObject != null) {
	  return unitObject;
	}
	try {
	  Constructor ec = cls.getConstructor();
	  unitObject = (C) ec.newInstance();
	  return unitObject;

	} catch(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
	  throw new RuntimeException("No default constructor in " + cls.getName() + " for unit()");
	}
  }

  public C createNew(PMap<String, Object> newProperties) {
	Objects.requireNonNull(newProperties, "newProperties");
	Object[] params = new Object[constructorProperties.size()];
	for(int t = 0; t < constructorProperties.size(); t++) {
	  Getter g = constructorProperties.get(t);

	  if(newProperties.containsKey(g.propertyName)) {
		params[t] = newProperties.get(g.propertyName);
	  }
	  else {
		params[t] = null;
	  }
	}
	try {
	  return (C) constructor.newInstance(params);
	} catch(Exception e) {
	  log.severe("Error constructing new " + cls.getName() + " with newProperties " + newProperties);
	  throw new RuntimeException("Error constructing new " + cls.getName() + " with newProperties " + newProperties);
	}
  }

  public PStream<Getter> getConstructorProperties() {
	return PStream.from(constructorProperties);
  }

  public C copyWith(C orgObject, String n1, Object v1) {
	PMap<String, Object> m = new PMap<>();
	m = m.put(n1, v1);
	return copyWith(orgObject, m);
  }

  public C copyWith(C orgObject, PMap<String, Object> newProperties) {
	Objects.requireNonNull(orgObject, "orgObject");
	Objects.requireNonNull(newProperties, "newProperties");
	Object[] params = new Object[constructorProperties.size()];
	for(int t = 0; t < constructorProperties.size(); t++) {
	  Getter g = constructorProperties.get(t);

	  if(newProperties.containsKey(g.propertyName)) {
		params[t] = newProperties.get(g.propertyName);
	  }
	  else {
		params[t] = g.getter.get(orgObject);
	  }
	}
	try {
	  return (C) constructor.newInstance(params);
	} catch(InstantiationException | IllegalAccessException | InvocationTargetException e) {
	  throw new RuntimeException("Error constructing new " + cls
															   .getName() + " from original " + orgObject + " with newProperties " + newProperties);
	}
  }

  public C copyWith(C orgObject, String n1, Object v1, String n2, Object v2) {
	PMap<String, Object> m = PMap.empty();
	m = m.put(n1, v1).put(n2, v2);
	return copyWith(orgObject, m);
  }

  public C copyWith(C orgObject, String n1, Object v1, String n2, Object v2, String n3, Object v3) {
	PMap<String, Object> m = PMap.empty();
	m = m.put(n1, v1).put(n2, v2).put(n3, v3);
	return copyWith(orgObject, m);
  }

  public Optional<Method> getGetterMethod(String fieldName) {
	Class<?> cls  = this.cls;
	String   get1 = "get" + firstCharUppercase(fieldName);
	String   get2 = "is" + firstCharUppercase(fieldName);


	while(cls != null && cls.equals(Object.class) == false) {
	  for(Method m : cls.getDeclaredMethods()) {
		String name = m.getName();
		if(!name.equals(get1) && !name.equals(get2)) {
		  continue;
		}
		return Optional.of(m);
	  }
	  cls = cls.getSuperclass();
	}
	return Optional.empty();
  }

  public Optional<Method> getWithMethod(Field f) {
	Class<?> cls  = this.cls;
	String   get1 = "with" + firstCharUppercase(f.getName());


	while(cls != null && cls.equals(Object.class) == false) {
	  for(Method m : cls.getDeclaredMethods()) {
		String name = m.getName();
		if(!name.equals(get1)) {
		  continue;
		}
		return Optional.of(m);
	  }
	  cls = cls.getSuperclass();
	}
	return Optional.empty();
  }

  public PStream<Getter> getFieldGetters() {
	return getters.values();
  }

  public void checkNullFields(C obj) {
	for(Getter g : getters.values()) {
	  if(g.isNullable == false) {
		if(g.getter.get(obj) == null) {
		  throw new IllegalStateException("Field " + g.propertyName + " in object of class " + this.cls
																								 .getName() + " is null!");
		}
	  }
	}
  }

  @SafeVarargs
  public final int hashCode(C obj, Lens<C, ?>... lenses) {
	int result = 1;
	for(Lens<C, ?> l : lenses) {
	  Object value = l.get(obj);
	  if(value != null) {
		result += 31 * value.hashCode();
	  }
	}
	return result;
  }

  /**
   * Calculate the hashcode using all the fields in the supplied object where there is no {@link NoEqual} annotation
   *
   * @param obj The Object to calculate the hashCode for
   *
   * @return The hashCode
   */
  public int hashCodeAll(C obj) {
	return hashCode(obj, getters.filter(g -> {
	  assert g._2 != null;
	  return g._2.field.getDeclaredAnnotation(NoEqual.class) == null;
	}).keys()
								.toArray(new String[0]));
  }

  public int hashCode(C obj, String... properties) {
	PStream<String> names  = (properties == null || properties.length == 0) ? getters.keys() : PStream.from(properties);
	int             result = 1;
	for(String prop : names) {
	  Object value = get(obj, prop);
	  if(value != null) {
		result += 31 * value.hashCode();
	  }
	}
	return result;
  }

  public Object get(C container, String name) {
	return getters.get(name).getter.get(container);
  }

  /**
   * Create a string using all the toStrings of the fields in the supplied object where there is no {@link NoToString} annotation.
   * Also shows null values.<br>
   *
   * @param obj The Object to create a string for
   *
   * @return The String representation
   *
   * @see #toStringAll(Object, boolean)
   * @see #toStringAllIgnoreNulls(Object)
   */
  public String toStringAll(C obj) {
	return toStringAll(obj, false);
  }

  /**
   * Create a string using all the toStrings of the fields in the supplied object where there is no {@link NoToString} annotation.
   *
   * @param obj        The Object to create a string for
   * @param ignoreNull ignore fields that have a null value
   *
   * @return The String representation
   */
  private String toStringAll(C obj, boolean ignoreNull) {
	String  result = cls.getSimpleName() + "[";
	boolean first  = true;
	for(String prop : getters.keys()) {
	  if(getters.get(prop).field.getDeclaredAnnotation(NoToString.class) != null) {
		continue;
	  }
	  Object value = get(obj, prop);
	  if(value == null && ignoreNull) {
		continue;
	  }
	  if(first == false) {
		result += ", ";
	  }
	  else {
		first = false;
	  }
	  result += prop + "=" + value;
	}
	return result + "]";
  }

  /**
   * Create a string using all the toStrings of the fields in the supplied object where there is no {@link NoToString} annotation.
   * Does not show fields with null values
   * @param obj        The Object to create a string for
   *
   * @return The String representation
   */
  public String toStringAllIgnoreNulls(C obj) {
	return toStringAll(obj, true);
  }

  public String toString(C obj, String n1, Lens<C, ?> l1) {
	return toString(obj, new Pair<>(n1, l1));
  }

  @SafeVarargs
  public final String toString(C obj, Pair<String, Lens<C, ?>>... lensNames) {
	String  result = cls.getSimpleName() + "[";
	boolean first  = true;
	for(Pair<String, Lens<C, ?>> p : lensNames) {
	  if(first) {
		first = false;
	  }
	  else {
		result += ", ";
	  }
	  String name = p.getLeft();
	  if(name != null && name.isEmpty() == false) {
		result += name + " = ";
	  }
	  Object v1 = p.getRight().get(obj);
	  if(v1 instanceof String) {
		v1 = "\"" + v1 + "\"";
	  }
	  result += String.valueOf(v1);
	}

	return result + "]";
  }

  public String toString(C obj, String n1, Lens<C, ?> l1, String n2, Lens<C, ?> l2) {
	return toString(obj, new Pair<>(n1, l1), new Pair<>(n2, l2));
  }

  public String toString(C obj, String n1, Lens<C, ?> l1, String n2, Lens<C, ?> l2, String n3, Lens<C, ?> l3) {
	return toString(obj, new Pair<>(n1, l1), new Pair<>(n2, l2), new Pair<>(n3, l3));
  }

  public String toString(C obj, String n1, Lens<C, ?> l1, String n2, Lens<C, ?> l2, String n3, Lens<C, ?> l3, String n4,
						 Lens<C, ?> l4
  ) {
	return toString(obj, new Pair<>(n1, l1), new Pair<>(n2, l2), new Pair<>(n3, l3), new Pair<>(n4, l4));
  }

  public String toString(C obj, String n1, Lens<C, ?> l1, String n2, Lens<C, ?> l2, String n3, Lens<C, ?> l3, String n4,
						 Lens<C, ?> l4, String n5, Lens<C, ?> l5
  ) {
	return toString(obj, new Pair<>(n1, l1), new Pair<>(n2, l2), new Pair<>(n3, l3), new Pair<>(n4, l4), new Pair<>(n5, l5));
  }

  @SuppressWarnings("unchecked")
  public boolean equals(C left, Object right, Lens<C, ?>... lenses) {
	if(right == null || right.getClass().equals(left.getClass())) {
	  return false;
	}
	for(Lens<C, ?> l : lenses) {
	  Object v1 = l.get(left);
	  Object v2 = l.get((C) right);
	  if(v1 == null) {
		if(v2 != null) {
		  return false;
		}
	  }
	  else {
		if(v1.equals(v2) == false) {
		  return false;
		}
	  }

	}
	return true;
  }

  /**
   * Compare 2 objects by comparing all the fields in the supplied left object where there is no {@link NoEqual} annotation
   *
   * @param left  The left field to compare
   * @param right The right field to compare
   *
   * @return is equal
   */
  @SuppressWarnings("unchecked")
  public boolean equalsAll(C left, Object right) {
	if(right == null || right.getClass().equals(left.getClass()) == false) {
	  return false;
	}
	for(String prop : getters.filter(g -> {
	  assert g._2 != null;
	  return g._2.field.getDeclaredAnnotation(NoEqual.class) == null;
	}).keys()) {
	  Object v1 = get(left, prop);
	  Object v2 = get((C) right, prop);
	  if(v1 == null && v2 != null) {
		return false;
	  }
	  if(v1 != null && v1.equals(v2) == false) {
		return false;
	  }
	}
	return true;
  }

  @SuppressWarnings("unchecked")
  public boolean equals(C left, Object right, String... properties) {
	if(right == null || right.getClass().equals(left.getClass())) {
	  return false;
	}
	//Collection<String> names = (properties == null || properties.length==0 )? getters.keySet() :  Arrays.asList(properties);
	PStream<String> names = (properties == null || properties.length == 0) ? getters.keys() : PStream.from(properties);
	for(String prop : names) {
	  Object v1 = get(left, prop);
	  Object v2 = get((C) right, prop);
	  if(v1 == null && v2 != null) {
		return false;
	  }
	  if(v1 == null) { return true; }
	  if(v1.equals(v2) == false) {
		return false;
	  }
	}
	return true;
  }

  public <X> Lens<C, X> lens(String propertyName) {
	if(getters.containsKey(propertyName) == false) {
	  throw new RuntimeException("Creating lens with name " + propertyName + " on class " + cls
																							  .getName() + " failed: property does not exists");
	}
	return new ImLens<>(propertyName);
  }

  public static class Getter{

	public final PropertyGetter getter;
	public final String         propertyName;
	public final boolean        isNullable;
	public final Field          field;

	@SuppressWarnings("BooleanParameter")
	public Getter(PropertyGetter getter, String propertyName, boolean isNullable, Field f) {
	  this.getter = getter;
	  this.propertyName = propertyName;
	  this.isNullable = isNullable;
	  this.field = f;
	}

	@Override
	public String toString() {
	  return "FieldGetter[" + propertyName + "]";
	}
  }


}
