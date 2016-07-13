package com.persistentbit.core.codegen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Tels the code generator {@link CaseClaseCodeBuilder} that the field is not to be included in the equals/hashcode
 * function if it is set on a field.<br>
 * If it is set on a class, then no equals/hashcode functions are generated.
 * User: petermuys
 * Date: 11/07/16
 * Time: 17:35
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GenNoEquals {
}
