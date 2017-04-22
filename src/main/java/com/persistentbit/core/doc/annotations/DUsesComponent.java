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
@Repeatable(DUsesComponents.class)
public @interface DUsesComponent{
	String name();
	String packageName();
	String label() default "";
	String thisLabel() default "";
	String otherLabel() default "";
}
