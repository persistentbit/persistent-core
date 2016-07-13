package com.persistentbit.core.codegen;

import com.persistentbit.core.Immutable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Class as a case class.<br>
 * A case class is like in scala, an immutable values class<br>
 * Case classes are automaticly candidates for Code generation via {@link CaseClaseCodeBuilder}
 * @author Peter Muys
 * @since 13/07/2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Immutable
public @interface CaseClass {
}
