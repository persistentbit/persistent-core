package com.persistentbit.core.parser;

import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.ToDo;

import java.util.Objects;
import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 17/02/17
 */
public abstract class ParseResult<T>{

	private ParseResult(){}

	public static <R> ParseFailure<R> failure(Source source, String errorMessage) {
		return new ParseFailure<>(source, new ParseException(errorMessage, source.position));
	}

	public static <R> ParseFailure<R> failure(Source source, ParseException parseException) {
		return new ParseFailure<>(source, parseException);
	}

	public static <R> ParseSuccess<R> success(Source source, R value) {
		return new ParseSuccess<>(source, value);
	}

	public abstract T getValue();

	public abstract ParseException getError();

	public abstract Source getSource();

	public <U> U match(
		Function<ParseSuccess<T>,U> success,
		Function<ParseFailure<T>,U> failure
	){
		throw new ToDo();
	}

	public abstract boolean isSuccess();
	public boolean isFailure() {
		return !isSuccess();
	}

	public abstract <R> ParseResult<R> map(Function<T, R> mapper);

	public abstract ParseResult<T> mapSource(Function<Source, Source> sourceMapper);

	public abstract ParseResult<T> onErrorAdd(String errorMessage);

	public abstract ParseResult<T> onErrorAdd(ParseException exception);

	public Result<T> asResult() {
		if(isSuccess()) {
			return Result.success(getValue());
		}
		return Result.failure(getError());
	}


	public static class ParseSuccess<T> extends ParseResult<T>{

		private final Source source;
		private final T value;

		public ParseSuccess(Source source, T value) {
			this.source = Objects.requireNonNull(source);
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public boolean isSuccess() {
			return true;
		}

		public Source getSource() {
			return source;
		}

		@Override
		public <R> ParseResult<R> map(Function<T, R> mapper) {
			return new ParseSuccess<>(source, mapper.apply(value));
		}

		@Override
		public T getValue() {
			return value;
		}

		@Override
		public ParseResult<T> mapSource(Function<Source, Source> sourceMapper
		) {
			return new ParseSuccess<>(sourceMapper.apply(source), value);
		}

		@Override
		public String toString() {
			return "Success[" + value + "]";
		}

		@Override
		public ParseException getError() {
			return new ParseException("Can't get Error on a success", source.position);
		}

		@Override
		public ParseResult<T> onErrorAdd(String errorMessage) {
			return this;
		}

		@Override
		public ParseResult<T> onErrorAdd(ParseException exception) {
			return this;
		}
	}

	public static class ParseFailure<T> extends ParseResult<T>{

		private final Source source;
		private final ParseException errorMessage;


		public ParseFailure(Source source, ParseException errorMessage) {
			this.source = source;
			this.errorMessage = errorMessage;

		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		public Source getSource() {
			return source;
		}

		@Override
		public <R> ParseResult<R> map(Function<T, R> mapper) {
			return new ParseFailure<>(source, errorMessage);
		}

		@Override
		public T getValue() {
			throw new RuntimeException(source.position + ": Can't get value from a failure", errorMessage);
		}

		@Override
		public String toString() {
			return "Failure[" + errorMessage.getMessage() + "]";
		}

		@Override
		public ParseException getError() {
			return errorMessage;
		}

		@Override
		public ParseResult<T> mapSource(Function<Source, Source> sourceMapper
		) {
			return new ParseFailure<>(sourceMapper.apply(source), errorMessage);
		}

		@Override
		public ParseResult<T> onErrorAdd(String errorMessage) {
			return onErrorAdd(new ParseException(errorMessage, source.position));
		}

		@Override
		public ParseResult<T> onErrorAdd(ParseException exception) {
			return new ParseFailure<>(source, this.errorMessage.withCause(exception));
		}
	}
}
