package com.persistentbit.core.doc.annotations;

import java.lang.annotation.*;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/04/17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Repeatable(DUsedByClasses.class)
public @interface DUsedByClass{
	Class value();
	String label() default "";
	String thisLabel() default "";
	String otherLabel() default "";
}
