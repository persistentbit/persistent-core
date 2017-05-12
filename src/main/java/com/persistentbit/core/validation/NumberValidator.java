package com.persistentbit.core.validation;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.language.Msg;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/02/17
 */
public class NumberValidator{

	public static SimpleValidator<Integer> minimum(int minimum) {
		return v -> v < minimum
			? PList.val(Msg.en("Minimum is {0}", minimum)) : PList.empty();
	}

	public static SimpleValidator<Integer> maximum(int maximum) {
		return v -> v > maximum
			? PList.val(Msg.en("Maximum is {0}", maximum)) : PList.empty();
	}

	public static SimpleValidator<Integer> range(int min, int max){
		return minimum(min).and(maximum(max));
	}
	public static SimpleValidator<Integer> positive(){
		return v -> v< 0
			? PList.val(Msg.en("Expected a positive integer"))
			: PList.empty();
	}

	public static SimpleValidator<Number> intNot0() {
		return v -> v.doubleValue() != 0.0
			? PList.val(Msg.en("Can't be 0")) : PList.empty();
	}
}
