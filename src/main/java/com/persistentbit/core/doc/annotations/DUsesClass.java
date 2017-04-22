package com.persistentbit.core.doc.annotations;

import java.lang.annotation.*;

/**
 * TODOC
 *
 * @author petermuys
 * @since 19/04/17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Repeatable(DUsesClasses.class)
public @interface DUsesClass{
	Class value();
	String label() default "";
	String thisLabel() default "";
	String otherLabel() default "";
}
