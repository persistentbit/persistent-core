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
		throw new ToDo();
	}
	static <R> ParseSuccess<R> success(ParseSource source, R value) {
		throw new ToDo();
	}

	public T getValue(){
		throw new ToDo();
	}

	public String getError() {
		throw new ToDo();
	}

	public ParseSource getSource() {
		throw new ToDo();
	}

	public <U> U match(
		Function<ParseSuccess<T>,U> success,
		Function<ParseFailure<T>,U> failure
	){
		throw new ToDo();
	}

	public boolean isSuccess(){
		throw new ToDo();
	}
	public boolean isFailure() {
		return !isSuccess();
	}

	public <R> ParseResult	map(Function<T,R> mapper){
		throw new ToDo();
	}

	public static class ParseSuccess<T> extends ParseResult<T>{

	}
	public static class ParseFailure<T> extends ParseResult<T>{

	}


}
