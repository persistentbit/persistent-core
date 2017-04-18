package com.persistentbit.core.doc.uml;

import java.lang.annotation.*;

/**
 * TODOC
 *
 * @author petermuys
 * @since 17/04/17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE,ElementType.TYPE})
public @interface UmlPackage{
	String name() default "";
	String info() default "";
	String note() default "";
}
