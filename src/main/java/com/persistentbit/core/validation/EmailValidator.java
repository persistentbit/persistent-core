package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.language.Msg;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/02/17
 */
public final class EmailValidator{

	public static final SimpleValidator<String> stringValidator =
		email -> {
			PList<Msg> res = StringValidator.mustContainAll(true, "@", ".").validate(email);
			if(res.isEmpty()) {
				String lastPart = email.substring(email.lastIndexOf('.') + 1);
				if(lastPart.length() >= 2 && email.indexOf("@") > 0) {
					return PList.empty();
				}
			}
			return PList.val(Msg.en("Not a valid email address"));
		};
}
