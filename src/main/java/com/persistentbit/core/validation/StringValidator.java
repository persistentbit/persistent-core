package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/02/17
 */
public class StringValidator{

	public static SimpleValidator<String> minLength(int minimumLength) {
		return v -> v.length() < minimumLength
			? PList.val("Minimum length is " + minimumLength) : PList.empty();
	}

	public static SimpleValidator<String> maxLength(int maxLength) {
		return v -> v.length() > maxLength
			? PList.val("Maximum length is " + maxLength) : PList.empty();
	}

	public static SimpleValidator<String> length(int minimumLength, int maxLength) {
		return minLength(minimumLength).and(maxLength(maxLength));
	}

	public static SimpleValidator<String> length(int fixedLength) {
		return v -> v.length() != fixedLength
			? PList.val("Length must be " + fixedLength) : PList.empty();
	}


	public static SimpleValidator<String> notEmpty() {
		return v -> minLength(1).validate(v.trim());
	}
}
