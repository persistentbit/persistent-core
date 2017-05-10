package com.persistentbit.core.doc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 4/05/2017
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.PACKAGE,ElementType.FIELD,ElementType.METHOD})
public @interface DTags {
    DTag[] value();
}
