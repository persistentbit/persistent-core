package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/02/17
 */
@FunctionalInterface
public interface SimpleValidator<S>{

	PList<String> validate(S value);

	default SimpleValidator<S> and(SimpleValidator<S> nextValidator) {
		return value -> validate(value).plusAll(nextValidator.validate(value));
	}


	default Validator<S> toValidator() {
		return (name, value) -> {
			PList<String> validateResult = validate(value);
			return validate(value).map(error -> new ValidationResult(name, error));
		};
	}


}
