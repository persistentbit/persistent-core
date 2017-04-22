package com.persistentbit.core.doc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODOC
 *
 * @author petermuys
 * @since 22/04/17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DUsesClasses{
	DUsesClass[] value();
}
