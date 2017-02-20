package com.persistentbit.core.utils;

import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Comparator;

/**
 * General utility functions for Java Numbers
 *
 * @author Peter Muys
 * @since 28/12/2016
 */
public final class NumberUtils {

	public static Result<Integer> parseInt(String str) {
		if(str == null){
			return Result.<Integer>failure("string is null").logFunction();
		}
		try{
			return Result.success(Integer.parseInt(str)).logFunction(str);
		}catch (RuntimeException e){
			return Result.<Integer>failure(e).logFunction(str);
		}

	}

	public static Result<Integer> parseHexInt(String str) {
		if(str == null) {
			return Result.<Integer>failure("string is null").logFunction();
		}
		try {
			return Result.success(Integer.parseInt(str, 16)).logFunction(str);
		} catch(RuntimeException e) {
			return Result.<Integer>failure(e).logFunction(str);
		}

	}


	public static Result<Long> parseLong(String str) {
		if(str == null) {
			return Result.<Long>failure("string is null").logFunction(str);
		}
		try {
			return Result.success(Long.parseLong(str)).logFunction(str);
		} catch(RuntimeException e) {
			return Result.<Long>failure(e).logFunction(str);
		}
	}

	public static Result<Double> parseDouble(String str) {
		if(str == null) {
			return Result.<Double>failure("string is null").logFunction(str);
		}
		try {
			return Result.success(Double.parseDouble(str)).logFunction(str);
		} catch(RuntimeException e) {
			return Result.<Double>failure(e).logFunction(str);
		}
	}

	public static Result<BigDecimal> parseBigDecimal(String str) {
		if(str == null) {
			return Result.<BigDecimal>failure("string is null").logFunction(str);
		}
		try {
			return Result.success(new BigDecimal(str)).logFunction(str);
		} catch(final NumberFormatException e) {
			return Result.<BigDecimal>failure(e).logFunction(str);
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
		if(number == null) {
			return Result.<BigDecimal>failure("number should not be null").logFunction(number);
		}

		if(number instanceof BigDecimal) {
			return Result.success((BigDecimal) number).logFunction(number);
		}

		if(number instanceof BigInteger) {
			return Result.success(new BigDecimal((BigInteger) number)).logFunction(number);
		}
		if(number instanceof Byte
				|| number instanceof Short
				|| number instanceof Integer
				|| number instanceof Long
				) {
			return Result.success(new BigDecimal(number.longValue())).logFunction(number);
		}
		if(number instanceof Float || number instanceof Double) {
			return Result.success(new BigDecimal(number.doubleValue())).logFunction(number);
		}
		return parseBigDecimal(number.toString()).logFunction(number);
	}

	/**
	 * Convert a computer size into a human readable String
	 * with 'kB','MB',...'TB'  units
	 * @param size The size in bytes
	 * @return The readable version.
	 */
	public static String	readableComputerSize(long size){
		if(size <= 0) return "0";
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size)/Math.log10(1000));
		return new DecimalFormat("#,##0.#").format(size/Math.pow(1000, digitGroups)) + " " + units[digitGroups];
	}

}
