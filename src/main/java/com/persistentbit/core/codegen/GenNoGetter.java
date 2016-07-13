package com.persistentbit.core.codegen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tels the code generator {@link CaseClaseCodeBuilder} to not generate a getter for a field<br>
 * If it is set on the class, then no getters are generated for tho whole class.
 * User: petermuys
 * Date: 11/07/16
 * Time: 17:27
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GenNoGetter {
}
