package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/02/17
 */
public class NotNullValidator<T> implements Validator<T>{

	@Override
	public PList<ValidationResult> validate(String name, T item) {
		return item == null
			? PList.val(new ValidationResult(name, ValidationResult.itemIsNull))
			: PList.empty();
	}

	@Override
	public <S> Validator<T> and(Function<T, S> subGetter, String subName, Validator<S> subValidator) {
		return (name, item) -> {
			PList<ValidationResult> res = validate(name, item);
			if(res.isEmpty() == false) {
				return res;
			}
			PList<ValidationResult> subResult = subValidator.validate(subName, subGetter.apply(item));
			return res.plusAll(subResult.map(vr -> vr.mapName(n -> name + "." + subName + "." + n)));
		};
	}
}
