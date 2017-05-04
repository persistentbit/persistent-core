package com.persistentbit.core.doc.annotations;

import java.lang.annotation.*;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 4/05/2017
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.PACKAGE,ElementType.FIELD,ElementType.METHOD})
@Repeatable(DTags.class)
public @interface DTag {
    String value();
}
