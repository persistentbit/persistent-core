package com.persistentbit.core.doc;

import java.lang.annotation.*;

/**
 * Marks a class or package as a package as a component.<br>
 * Classes or packages marked with this annotation will be used by documentation generators.<br>
 *
 * @author petermuys
 * @since 31/03/17
 * @see Support
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE,ElementType.TYPE})
public @interface Component{
	String name() default  "";
	String description() default "";
}
