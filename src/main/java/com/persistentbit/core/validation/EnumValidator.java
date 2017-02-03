package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.language.Msg;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/02/17
 */
public class EnumValidator{

	public static <E extends Enum<E>> SimpleValidator<E> oneOf(E... values) {
		return v -> PStream.val(values).contains(v)
			? PList.val(Msg.en("Expected one of {0}", values))
			: PList.empty();
	}

	public static <E extends Enum<E>> SimpleValidator<E> notOneOf(E... values) {
		return v -> PStream.val(values).contains(v) == false
			? PList.val(Msg.en("Dit not expected one of {0}", values))
			: PList.empty();
	}
}
