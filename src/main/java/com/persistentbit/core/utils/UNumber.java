package com.persistentbit.core.utils;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.tuples.Tuple2;

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
public final class UNumber{

	public static Result<Byte> convertToByte(Number value) {
		if(value == null) {
			return Result.<Byte>empty().logFunction(value);
		}
		if(value instanceof Byte) {
			return Result.success((Byte) value);
		}
		return convertToInteger(value)
			.verify(ival -> ival >= Byte.MIN_VALUE && ival <= Byte.MAX_VALUE, "value out of range for byte: " + value)
			.map(ival -> ival.byteValue());
	}

	public static Result<Short> convertToShort(Number value) {
		if(value == null) {
			return Result.<Short>empty().logFunction(value);
		}
		if(value instanceof Short) {
			return Result.success((Short) value);
		}
		return convertToInteger(value)
			.verify(ival -> ival >= Short.MIN_VALUE && ival <= Short.MAX_VALUE, "value out of range for short: " + value)
			.map(ival -> ival.shortValue());
	}

	public static Result<Integer> convertToInteger(Number value) {
		if(value == null) {
			return Result.<Integer>empty().logFunction(value);
		}
		if(value instanceof Integer) {
			return Result.success((Integer) value);
		}
		return convertToLong(value)
			.verify(ival -> ival >= Integer.MIN_VALUE && ival <= Integer.MAX_VALUE, "value out of range for integer: " + value)
			.map(ival -> ival.intValue());
	}

	public static Result<Long> convertToLong(Number value) {
		if(value == null) {
			return Result.<Long>empty().logFunction(value);
		}
		if(value instanceof Long) {
			return Result.success((Long) value);
		}
		if(isNaturalNumber(value)) {
			return Result.success(value.longValue());
		}
		return Result.failure("Not a natural number:" + value);
	}

	public static Result<Float> convertToFloat(Number value) {
		if(value == null) {
			return Result.<Float>empty().logFunction(value);
		}
		if(value instanceof Float) {
			return Result.success((Float) value);
		}
		if(value instanceof Double) {
			return Result.failure("Can't convert a double to a float:" + value);
		}
		return Result.success(value.floatValue());
	}

	public static Result<Double> convertToDouble(Number value) {
		if(value == null) {
			return Result.<Double>empty().logFunction(value);
		}
		if(value instanceof Double) {
			return Result.success((Double) value);
		}
		if(value instanceof Float) {
			return Result.success(value.doubleValue());
		}
		return Result.success(value.doubleValue());
	}

	public static <R> Result<R> convertTo(Number value, Class<R> cls) {
		if(value == null) {
			return Result.<R>empty().logFunction(value);
		}
		if(cls == byte.class || cls == Byte.class) {
			return (Result<R>) convertToByte(value);
		}
		if(cls == short.class || cls == Short.class) {
			return (Result<R>) convertToShort(value);
		}
		if(cls == int.class || cls == Integer.class) {
			return (Result<R>) convertToInteger(value);
		}
		if(cls == long.class || cls == Long.class) {
			return (Result<R>) convertToLong(value);
		}
		if(cls == float.class || cls == Float.class) {
			return (Result<R>) convertToFloat(value);
		}
		if(cls == double.class || cls == Double.class) {
			return (Result<R>) convertToDouble(value);
		}
		return Result
			.failure("Don't know how to convert a " + value.getClass().getSimpleName() + " to a" + cls.getName());
	}

	private static PMap<Tuple2<Class, Class>, Class> unifyLookup = PMap.<Tuple2<Class, Class>, Class>empty()
		.put(Tuple2.of(Byte.class, Short.class), Short.class)
		.put(Tuple2.of(Byte.class, Integer.class), Integer.class)
		.put(Tuple2.of(Byte.class, Long.class), Long.class)
		.put(Tuple2.of(Byte.class, Float.class), Float.class)
		.put(Tuple2.of(Byte.class, Double.class), Double.class)
		.put(Tuple2.of(Short.class, Byte.class), Short.class)
		.put(Tuple2.of(Short.class, Integer.class), Integer.class)
		.put(Tuple2.of(Short.class, Long.class), Long.class)
		.put(Tuple2.of(Short.class, Float.class), Float.class)
		.put(Tuple2.of(Short.class, Double.class), Double.class)
		.put(Tuple2.of(Integer.class, Byte.class), Integer.class)
		.put(Tuple2.of(Integer.class, Short.class), Integer.class)
		.put(Tuple2.of(Integer.class, Long.class), Long.class)
		.put(Tuple2.of(Integer.class, Float.class), Float.class)
		.put(Tuple2.of(Integer.class, Double.class), Double.class).put(Tuple2.of(Long.class, Byte.class), Long.class)
		.put(Tuple2.of(Long.class, Short.class), Long.class)
		.put(Tuple2.of(Long.class, Integer.class), Long.class)
		.put(Tuple2.of(Long.class, Float.class), Float.class)
		.put(Tuple2.of(Long.class, Double.class), Double.class)
		.put(Tuple2.of(Float.class, Float.class), Float.class)
		.put(Tuple2.of(Float.class, Double.class), Double.class);

	public static Result<Tuple2<Number, Number>> unify(Number left, Number right) {
		if(left == null || right == null) {
			return Result.failure("Can't unify a null value");
		}
		Class clsLeft  = left.getClass();
		Class clsRight = right.getClass();
		if(clsLeft == clsRight) {
			return Result.success(Tuple2.of(left, right));
		}
		return Result.fromOpt(unifyLookup.getOpt(Tuple2.of(clsLeft, clsRight)))
					 .flatMap(clsUni ->
						 convertTo(left, clsUni)
							 .flatMap(nl -> convertTo(right, clsUni)
								 .map(nr -> Tuple2.of(nl, nr))
							 )
					 )
			;
	}


	public static boolean isNaturalNumber(Number n) {
		if(n == null) {
			return false;
		}
		Class cls = n.getClass();
		return isNaturalNumberClass(cls);
	}

	public static boolean isNaturalNumberClass(Class cls) {
		return
			cls == Byte.class || cls == byte.class ||
				cls == Short.class || cls == short.class ||
				cls == Integer.class || cls == int.class ||
				cls == Long.class || cls == long.class ||
				cls == BigInteger.class
			;
	}

	public static boolean isDecimalNumberClass(Class cls) {
		return
			cls == Float.class || cls == float.class ||
				cls == Double.class || cls == double.class ||
				cls == BigDecimal.class;
	}

	public static boolean isNumberClass(Class cls) {
		return isNaturalNumberClass(cls) || isDecimalNumberClass(cls);

	}

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
