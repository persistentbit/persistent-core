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

	static <R> ParseFailure<R>	failure(ParseSource source, String errorMessage){
		return new ParseFailure<>(source, errorMessage);
	}
	static <R> ParseSuccess<R> success(ParseSource source, R value) {
		return new ParseSuccess<>(source, value);
	}

	public abstract T getValue();

	public String getError() {
		throw new ToDo();
	}

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
	}

	public static class ParseFailure<T> extends ParseResult<T>{

		private final ParseSource source;
		private final String errorMessage;

		public ParseFailure(ParseSource source, String errorMessage) {
			this.source = source;
			this.errorMessage = errorMessage;
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
			throw new RuntimeException(source.getPosition() + ": " + errorMessage);
		}

		@Override
		public ParseResult<T> mapSource(Function<ParseSource, ParseSource> sourceMapper
		) {
			return new ParseFailure<>(sourceMapper.apply(source), errorMessage);
		}
	}
}
