package com.persistentbit.core.doc.annotations;

import java.lang.annotation.*;

/**
 * TODOC
 *
 * @author petermuys
 * @since 26/04/17
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DCreates{
	Class value();
	String label() default "creates";
	String thisLabel() default "";
	String otherLabel() default "";
}