package com.persistentbit.core.doc.annotations;

import java.lang.annotation.*;

/**
 * TODOC
 *
 * @author petermuys
 * @since 22/04/17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Repeatable(DAggregateOfMultiple.class)
public @interface DAggregateOf{
	Class value();
	String label() default "";
	String thisLabel() default "";
	String otherLabel() default "";
}
