package com.persistentbit.core.validation;

import com.persistentbit.core.utils.BaseValueClass;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/02/17
 */
public class ValidationResult extends BaseValueClass{

	private final String name;
	private final String errorMessage;

	public ValidationResult(String name, String errorMessage) {
		this.name = name;
		this.errorMessage = errorMessage;
	}

	public ValidationResult mapName(Function<String, String> nameMapper) {
		return new ValidationResult(nameMapper.apply(name), errorMessage);
	}

	@Override
	public String toString() {
		return "(" + name + ": " + errorMessage + ")";
	}
}