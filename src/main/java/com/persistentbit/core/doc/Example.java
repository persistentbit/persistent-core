package com.persistentbit.core.doc;

import java.lang.annotation.*;

/**
 * Marks a class or a method as Example code.
 *
 * @author petermuys
 * @since 4/04/17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Example{
}
