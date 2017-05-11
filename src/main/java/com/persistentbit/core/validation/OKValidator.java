package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 11/05/2017
 */
public class OKValidator<T> implements Validator<T> {
    private static final OKValidator inst = new OKValidator();
    public static <T> OKValidator<T> inst(){
        return (OKValidator<T>)inst;
    }
    private OKValidator() {

    }
    @Override
    public PList<ValidationResult> validate(String name, T item) {
        return PList.empty();
    }
}
