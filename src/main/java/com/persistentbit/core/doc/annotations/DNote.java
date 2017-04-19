package com.persistentbit.core.doc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODOC
 *
 * @author petermuys
 * @since 19/04/17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE,ElementType.TYPE})
public @interface DNote{
	String value();
}
