package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.language.Msg;
import com.persistentbit.core.result.Result;

import java.util.Optional;
import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/02/17
 */
@FunctionalInterface
public interface Validator<T>{

	PList<ValidationResult> validate(String name, T item);

	default Result<T> validateToResult(String name, T item) {
		PList<ValidationResult> result = validate(name, item);
		if(result.isEmpty()) {
			return Result.result(item);
		}
		return Result.failure(result.toString(", "));
	}

	default <S> Validator<T> and(Function<T, S> subGetter, String subName, Validator<S> subValidator) {
		return (name, item) -> {
			PList<ValidationResult> res       = validate(name, item);
			PList<ValidationResult> subResult = subValidator.validate(subName, subGetter.apply(item));
			return res.plusAll(subResult.map(vr -> vr.mapName(n -> name + "." + subName + "." + n)));
		};
	}
	default Validator<T> and(Validator<T> validator){
		return (name, item) -> {
			PList<ValidationResult> res      = validate(name, item);
			return res.plusAll(validator.validate(name, item));
		};
	}
	default Validator<T> and(SimpleValidator<T> validator){
		return and(validator.toValidator());
	}

	default <S> Validator<T> and(Function<T, S> subGetter, String subName, SimpleValidator<S> simpleValidator) {
		return (name, item) -> {
			PList<ValidationResult> res      = validate(name, item);
			S                       subValue = subGetter.apply(item);
			if(subValue == null) {
				return res.plus(new ValidationResult(name + "." + subName, Msg.en("Item is not defined")));
			}
			return res.plusAll(simpleValidator.toValidator().validate(name + "." + subName, subValue));
		};
	}


	default <S> Validator<T> andMaybeNull(Function<T, S> subGetter, String subName, SimpleValidator<S> simpleValidator
	) {
		return (name, item) -> {
			PList<ValidationResult> res      = validate(name, item);
			S                       subValue = subGetter.apply(item);
			if(subValue == null) {
				return res;
			}
			return res.plusAll(simpleValidator.toValidator().validate(name + "." + subName, subValue));
		};
	}

	default <S> Validator<T> andOptional(Function<T, Optional<S>> subGetter, String subName,
										 SimpleValidator<S> simpleValidator
	) {
		return andMaybeNull(v -> subGetter.apply(v).orElse(null), subName, simpleValidator);
	}

	static <T> Validator<T> notNull() {
		return new NotNullValidator<>();
	}

	static <T> Validator<T> maybeNull() {
		return new MaybeNullValidator<>();
	}


	static <ERR> PMap<String, PList<ERR>> validationResultAsErrorMap(PList<ValidationResult> validationResults,
																	 Function<Msg, ERR> errorMapper
	) {
		return validationResults
			.groupBy(ValidationResult::getName, vr -> errorMapper.apply(vr.getErrorMessage()));
	}
}
