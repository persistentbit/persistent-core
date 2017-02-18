package com.persistentbit.core.experiments.parser;

import com.persistentbit.core.utils.ToDo;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 17/02/17
 */
public abstract class ParseResult<T>{

	private ParseResult(){}

	public static <R> ParseFailure<R>	failure(ParseSource source, String errorMessage){
		return new ParseFailure<>(source, new RuntimeException(source.getPosition().toString() + ": " + errorMessage));
	}
	public static <R> ParseSuccess<R> success(ParseSource source, R value) {
		return new ParseSuccess<>(source, value);
	}

	public abstract T getValue();

	public abstract RuntimeException getError();

	public abstract ParseSource getSource();

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

	public abstract ParseResult<T> mapSource(Function<ParseSource, ParseSource> sourceMapper);

	public static class ParseSuccess<T> extends ParseResult<T>{

		private final ParseSource source;
		private final T value;

		public ParseSuccess(ParseSource source, T value) {
			this.source = source;
			this.value = value;
		}

		@Override
		public boolean isSuccess() {
			return true;
		}

		public ParseSource getSource() {
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
		public ParseResult<T> mapSource(Function<ParseSource, ParseSource> sourceMapper
		) {
			return new ParseSuccess<>(sourceMapper.apply(source), value);
		}

		@Override
		public RuntimeException getError() {
			return new RuntimeException("Can't get Error on a success");
		}
	}

	public static class ParseFailure<T> extends ParseResult<T>{

		private final ParseSource source;
		private final RuntimeException errorMessage;

		public ParseFailure(ParseSource source, RuntimeException errorMessage) {
			this.source = source;
			this.errorMessage = new RuntimeException(errorMessage);
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		public ParseSource getSource() {
			return source;
		}

		@Override
		public <R> ParseResult<R> map(Function<T, R> mapper) {
			return new ParseFailure<>(source, errorMessage);
		}

		@Override
		public T getValue() {
			throw new RuntimeException(source.getPosition() + ": Can't get value from a failure",errorMessage);
		}

		@Override
		public RuntimeException getError() {
			return errorMessage;
		}

		@Override
		public ParseResult<T> mapSource(Function<ParseSource, ParseSource> sourceMapper
		) {
			return new ParseFailure<>(sourceMapper.apply(source), errorMessage);
		}
	}
}
