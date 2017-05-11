package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.language.Msg;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/02/17
 */
@FunctionalInterface
public interface SimpleValidator<S>{

	PList<Msg> validate(S value);

	default SimpleValidator<S> and(SimpleValidator<S> nextValidator) {
		return value -> validate(value).plusAll(nextValidator.validate(value));
	}

	default SimpleValidator<S> mapErrors(Function<Msg, Msg> errorMapper) {
		SimpleValidator<S> self = this;
		return v -> self.validate(v).map(errorMapper::apply);
	}


	default Validator<S> toValidator() {
		return (name, value) -> {
			//PList<Msg> validateResult = validate(value);
			return validate(value).map(error -> new ValidationResult(name, error));
		};
	}

}
