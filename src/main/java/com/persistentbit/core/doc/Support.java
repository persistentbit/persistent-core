package com.persistentbit.core.doc;

import java.lang.annotation.*;


/**
 * Marks a class or package as a package as a component support class or package.<br>
 * Classes or packages marked with this annotation will be used by documentation generators.<br>
 *
 * @author petermuys
 * @since 31/03/17
 * @see Component
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE,ElementType.TYPE})
public @interface Support{
	String name()  default "";
	String description()  default "";
	String componentName() default "";
	String technology() default "java";
	String relation() default "Supported by";
}