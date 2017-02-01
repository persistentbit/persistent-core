package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/02/17
 */
public class MaybeNullValidator<T> implements Validator<T>{

	@Override
	public PList<ValidationResult> validate(String name, T item) {
		return PList.empty();
	}

	@Override
	public <S> Validator<T> and(Function<T, S> subGetter, String subName, Validator<S> subValidator) {
		return (name, item) -> {
			if(item == null) {
				return PList.empty();
			}
			PList<ValidationResult> subResult = subValidator.validate(subName, subGetter.apply(item));
			return subResult.map(vr -> vr.mapName(n -> name + "." + subName + "." + n));
		};
	}
}
