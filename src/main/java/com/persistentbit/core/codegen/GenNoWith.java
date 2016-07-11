package com.persistentbit.core.codegen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tels the code generator {@link ImmutableCodeBuilder} to not generate a with function for a field<br>
 * If it is set on the class, then no with functions are generated for tho whole class.
 * User: petermuys
 * Date: 11/07/16
 * Time: 17:28
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GenNoWith {
}
