package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.language.Msg;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/02/17
 */
public class StringValidator{

	public static SimpleValidator<String> minLength(int minimumLength) {
		return v -> v.length() < minimumLength
			? PList.val(Msg.en("Minimum length is {0}", minimumLength)) : PList.empty();
	}

	public static SimpleValidator<String> maxLength(int maxLength) {
		return v -> v.length() > maxLength
			? PList.val(Msg.en("Maximum length is {0}", maxLength)) : PList.empty();
	}

	public static SimpleValidator<String> length(int minimumLength, int maxLength) {
		return minLength(minimumLength).and(maxLength(maxLength));
	}

	public static SimpleValidator<String> length(int fixedLength) {
		return v -> v.length() != fixedLength
			? PList.val(Msg.en("Length must be {0}" + fixedLength)) : PList.empty();
	}

	public static SimpleValidator<String> mustContainAll(boolean caseSensitive, String... containValues) {
		return v -> {
			if(caseSensitive == false) {
				v = v.toUpperCase();
			}
			for(String contains : containValues) {
				boolean ok = caseSensitive
					? v.contains(contains)
					: v.contains(contains.toUpperCase());
				if(ok == false) {
					return PList.val(Msg.en("Must contain {0}", contains));
				}
			}
			return PList.empty();
		};
	}

	public static SimpleValidator<String> mustContainAny(boolean caseSensitive, String... containValues) {
		return v -> {
			if(caseSensitive == false) {
				v = v.toUpperCase();
			}
			for(String contains : containValues) {
				boolean ok = caseSensitive
					? v.contains(contains)
					: v.contains(contains.toUpperCase());
				if(ok) {
					return PList.empty();
				}
			}
			return PList.val(Msg.en("Must contain any of {0}", PStream.val(containValues)));
		};
	}

	public static SimpleValidator<String> regEx(String regEx) {
		return v -> v.matches(regEx) == false
			? PList.val(Msg.en("Invalid format"))
			: PList.empty();
	}

	public static SimpleValidator<String> notEmpty() {
		return v -> minLength(1).validate(v.trim());
	}
}
