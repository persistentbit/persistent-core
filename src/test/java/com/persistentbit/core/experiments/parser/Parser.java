package com.persistentbit.core.experiments.parser;

import com.persistentbit.core.OK;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.tuples.Tuple2;

import java.util.Objects;
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
		return andThen(Objects.requireNonNull(nextParser)).map(t -> t._2).parserName("skipAndThan");
	}
	default Parser<T> andThenSkip(Parser<?> nextParser){
		return andThen(nextParser).map(t -> t._1)
								  .parserName(this.toString() + ".andThenSkip(" + nextParser + "." + nextParser
									  .toString());
	}

	default Parser<T> skipWhiteSpace() {
		return whiteSpace().skipAndThen(this);
	}

	static Parser<String> whiteSpace() {
		return source -> {
			String res = "";
			while(Character.isWhitespace((char) source.current())) {
				res = res + ((char) source.current());
				source = source.next();
			}
			return ParseResult.success(source, res);
		};
	}

	default Parser<T> parserName(String name) {
		Parser<T> self = this;
		return new Parser<T>(){
			@Override
			public ParseResult<T> parse(ParseSource source) {
				return self.parse(source);
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}

	static Parser<OK> eof() {
		return source -> {
			if(source.current() == ParseSource.EOF) {
				return ParseResult.success(source, OK.inst);
			}
			return ParseResult.failure(source, "Expected end-of-file!");
		};
	}

	default Parser<T> andThenEof() {
		return andThenSkip(eof());
	}

	default <U> Parser<Tuple2<T, U>> andThen(Parser<U> nextParser) {
		Objects.requireNonNull(nextParser);
		Parser<T> self = this;
		Parser<Tuple2<T, U>> parser = source -> {
			ParseResult<T> thisResult = self.parse(source);
			if(thisResult.isFailure()){
				return thisResult.map(v -> null);
			}
			ParseResult<U> nextResult = nextParser.parse(thisResult.getSource());
			if(nextResult.isFailure()){
				return nextResult.map(v -> null);
			}
			return ParseResult.success(nextResult.getSource(),Tuple2.of(thisResult.getValue(),nextResult.getValue()));
		};
		return parser.parserName(this + ".andThen(" + nextParser + ")");
	}

	default <R> Parser<R> map(Function<T,R> mapper){
		Parser<T> self = this;
		return source ->  self.parse(source).map(mapper);

	}

	default Parser<Optional<T>> optional(){
		Parser<T> self = this;
		return Log.function().code(l -> source -> {
			ParseResult<T> res = self.parse(source.withSnapshot());
			if(res.isSuccess()) {
				return res.map(Optional::ofNullable).mapSource(ParseSource::resolved);
			}
			return ParseResult.success(res.getSource().getSnapshot(), Optional.empty());
		});
	}


	default Parser<T> or(Parser<T> other){
		Parser<T> self = this;
		return Log.function().code(l -> source -> {

			ParseResult<T> thisResult = self.parse(source.withSnapshot());
			if(thisResult.isSuccess()){
				return ParseResult.success(thisResult.getSource().resolved(),thisResult.getValue());
			}
			source = thisResult.getSource().getSnapshot();
			ParseResult<T> otherResult = other.parse(source);
			if(otherResult.isSuccess()) {
				return ParseResult.success(otherResult.getSource().resolved(), otherResult.getValue());
			}
			return ParseResult.failure(source.getSnapshot(), "No match beteen " + self + " and " + other);
		});
	}
}
