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
public class PhoneValidator{

	/**
	 * Validates a telephone number in ITU-T E.123 format (https://en.wikipedia.org/wiki/E.123)<br>
	 * Examples: <br>
	 * +1 1234567890123
	 * +12 123456789
	 * +123 123456
	 */
	public static final SimpleValidator<String> stringValidator = StringValidator.regEx("^\\+(?:[0-9] ?){6,14}[0-9]$");


	public static final SimpleValidator<String> stringCleanedValidator = (String v) -> {
		Result<String> resCleaned = cleanupPhoneNumber(v);
		PList<Msg>     clean      = ResultValidator.<String>isSuccess().validate(resCleaned);
		if(clean.isEmpty() == false) {
			return clean;
		}
		return stringValidator.validate(resCleaned.orElseThrow());
	};

	/**
	 * Cleanup a telephone number and try to return a valid ITU-T E.123.<br>
	 * The result is not validated to be a valid ITU-T E.123 format.
	 *
	 * @param phoneNumber The telephone number to clean
	 *
	 * @return The result of cleaning the number
	 */
	static public final Result<String> cleanupPhoneNumber(String phoneNumber) {
		return Result.function(phoneNumber).code(l -> {
			String res = phoneNumber;
			if(res == null) {
				return Result.failure("Phone number is null");
			}
			res = res.trim();
			if(res.startsWith("00")) {
				res = "+" + res.substring(2);
			}
			// +123 nr...
			// 0123456
			if(res.length() > 6) {
				res = res.substring(0, 6) + res.substring(6).replace(" ", "");
			}
			res = res.replace("(", "");
			res = res.replace(")", "");
			res = res.replace(".", "");
			res = res.replace("-", "");
			return Result.success(res);
		});
	}


}
