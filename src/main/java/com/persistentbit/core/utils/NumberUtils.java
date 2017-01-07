package com.persistentbit.core.utils;

import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 28/12/2016
 */
public final class NumberUtils {

	public static Result<Integer> parseInt(String str) {
		return Result.function(str).code(l -> {
			if(str == null){
				return Result.failure("string is null");
			}
			try{
				return Result.success(Integer.parseInt(str));
			}catch (RuntimeException e){
				return Result.failure(e);
			}
		});
	}

	public static Result<Long> parseLong(String str) {
		return Result.function(str).code(l -> {
			if(str == null) {
				return Result.failure("string is null");
			}
			try {
				return Result.success(Long.parseLong(str));
			} catch(RuntimeException e) {
				return Result.failure(e);
			}
		});
	}

	public static Result<BigDecimal> parseBigDecimal(String str) {
		if(str == null) {
			return Result.failure("string is null");
		}
		try {
			return Result.success(new BigDecimal(str));
		} catch(final NumberFormatException e) {
			return Result.failure(e);
		}

	}

	/**
	 * Comparator for Number instances of different type.
	 */
	public static final Comparator<Number> numberComparator = (Number left, Number right) ->
		Log.function(left, right).code(log ->
										   numberToBigDecimal(left)
											   .flatMap(l ->
															numberToBigDecimal(right).map(l::compareTo)
											   ).orElseThrow()
		);

	/**
	 * Check if a Number instance is a Double/Float Nan or infinite
	 *
	 * @param number The Number instance
	 *
	 * @return true if special number
	 */
	public static boolean isSpecialNumber(Number number) {
		if(number instanceof Double
			&& (Double.isNaN((Double) number) || Double.isInfinite((Double) number))) {
			return true;
		}
		return number instanceof Float
			&& (Float.isNaN((Float) number) || Float.isInfinite((Float) number));
	}

	/**
	 * Convert a Number instance to a BigDecimal representation.<br>
	 *
	 * @param number The Number to convert
	 *
	 * @return The resulting {@link BigDecimal}
	 */
	public static Result<BigDecimal> numberToBigDecimal(Number number) {
		return Result.function(number).code(l -> {
			if(number == null) {
				return Result.failure("number should not be null");
			}

			if(number instanceof BigDecimal) {
				return Result.success((BigDecimal) number);
			}

			if(number instanceof BigInteger) {
				return Result.success(new BigDecimal((BigInteger) number));
			}
			if(number instanceof Byte
				|| number instanceof Short
				|| number instanceof Integer
				|| number instanceof Long
				) {
				return Result.success(new BigDecimal(number.longValue()));
			}
			if(number instanceof Float || number instanceof Double) {
				return Result.success(new BigDecimal(number.doubleValue()));
			}
			return parseBigDecimal(number.toString());
		});
	}
}
