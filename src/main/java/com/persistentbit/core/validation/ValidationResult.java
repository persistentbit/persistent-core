package com.persistentbit.core.validation;

import com.persistentbit.core.language.Msg;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.Objects;
import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/02/17
 */
public class ValidationResult extends BaseValueClass{

	static final Msg itemIsNull = Msg.en("Item is not defined.");

	private final String name;
	private final Msg errorMessage;

	public ValidationResult(String name, Msg errorMessage) {
		this.name = Objects.requireNonNull(name);
		this.errorMessage = Objects.requireNonNull(errorMessage);
	}

	public ValidationResult mapName(Function<String, String> nameMapper) {
		return new ValidationResult(nameMapper.apply(name), errorMessage);
	}


	public String getName() {
		return name;
	}

	public Msg getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String toString() {
		return "(" + name + ": " + errorMessage + ")";
	}
}