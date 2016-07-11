package com.persistentbit.core.codegen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *  Tels the code generator {@link ImmutableCodeBuilder} to not generate a Lens for a field<br>
 * If it is set on the class, then no Lenses are generated for tho whole class.
 * User: petermuys
 * Date: 11/07/16
 * Time: 17:30
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GenNoLens {
}
