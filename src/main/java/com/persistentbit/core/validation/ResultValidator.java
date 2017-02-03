package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.language.Msg;
import com.persistentbit.core.result.Result;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/02/17
 */
public final class ResultValidator{


	public static <T> SimpleValidator<Result<T>> isSuccess() {
		return v ->
			v.match(
				onSuccess -> PList.empty(),
				onEmpty -> PList.val(Msg.en("No value: {0}", onEmpty.getException().getMessage())),
				onFailure -> PList.val(Msg.en("Error: {0}", onFailure.getException().getMessage()))
			)
			;
	}
}
