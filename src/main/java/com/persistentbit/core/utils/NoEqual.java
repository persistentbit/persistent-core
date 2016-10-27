package com.persistentbit.core.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field annotation for fields that should be excluded in {@link ImTools#hashCodeAll(Object)} and {@link ImTools#equalsAll(Object, Object)}
 *
 * @author Peter Muys
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface NoEqual{
}
