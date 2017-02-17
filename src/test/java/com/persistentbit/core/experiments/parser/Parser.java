package com.persistentbit.core.experiments.parser;

import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.ToDo;

import java.util.Optional;
import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 17/02/17
 */
@FunctionalInterface
public interface Parser<T>{
	ParseResult<T>	parse(ParseSource source);




	default <U> Parser<U> skipAndThen(Parser<U> nextParser){
		return andThen(nextParser).map(t -> t._2);
	}
	default Parser<T> andThenSkip(Parser<?> nextParser){
		return andThen(nextParser).map(t -> t._1);
	}

	default Parser<T> skipWhiteSpace() {
		return whiteSpace().skipAndThen(this);
	}

	static Parser<String> whiteSpace() {
		throw new ToDo();
	}

	default <U> Parser<Tuple2<T,U>> andThen(Parser<U> nextParser){
		Parser<T> self = this;
		return source -> {
			ParseResult<T> thisResult = self.parse(source);
			if(thisResult.isFailure()){
				return thisResult.map(v -> v);
			}
			ParseResult<U> nextResult = nextParser.parse(thisResult.getSource());
			if(nextResult.isFailure()){
				return nextResult.map(v -> v);
			}
			return ParseResult.success(nextResult.getSource(),Tuple2.of(thisResult.getValue(),nextResult.getValue()));
		};
	}

	default <R> Parser<R> map(Function<T,R> mapper){
		Parser<T> self = this;
		return source ->  self.parse(source).map(mapper);

	}

	default Parser<Optional<T>> optional(){
		throw new ToDo();
	}

	default Parser<T> or(Parser<T> other){
		throw new ToDo();
	}
}
