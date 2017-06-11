package com.persistentbit.core.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field annotation for fields that should be excluded in {@link ImTools#toStringAll(Object, boolean)}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.TYPE})
public @interface NoToString{
}
