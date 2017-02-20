package com.persistentbit.core.parser;

import com.persistentbit.core.OK;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.parser.source.Position;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.tuples.Tuple2;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * TODOC
 *
 * @author petermuys
 * @since 17/02/17
 */
@FunctionalInterface
public interface Parser<T>{

	ParseResult<T> parse(Source source);


	/**
	 * Execute this parser and then throw it away and parse the next part.
	 *
	 * @param nextParser The next part parser
	 * @param <U>        The resulting parser type
	 *
	 * @return The new parser
	 */
	default <U> Parser<U> skipAndThen(Parser<U> nextParser) {
		return andThen(Objects.requireNonNull(nextParser)).map(t -> t._2);
	}

	default Parser<T> andThenSkip(Parser<?> nextParser) {
		return  andThen(nextParser).map(t -> t._1);
	}


	default Parser<T> skip(Parser<?> skip) {
		return skip.skipAndThen(this);
	}





	default Parser<T> onErrorAddMessage(String errorMessage) {
		Parser<T> self = this;
		return source -> {
			ParseResult<T> res = self.parse(source);
			res = res.onErrorAdd(errorMessage);
			return res;
		};
	}

	default Parser<T> andThenEof() {
		return andThenSkip(Scan.eof);
	}


	default <U> Parser<Tuple2<T, U>> andThen(Parser<U> nextParser) {
		Objects.requireNonNull(nextParser);
		Parser<T> self = this;
		return source -> {
			ParseResult<T> thisResult = self.parse(source);
			if(thisResult.isFailure()) {
				return thisResult.map(v -> null);
			}
			ParseResult<U> nextResult = nextParser.parse(thisResult.getSource());
			if(nextResult.isFailure()) {
				return nextResult.map(v -> null);
			}
			return ParseResult.success(nextResult.getSource(), Tuple2.of(thisResult.getValue(), nextResult.getValue()));
		};
	}

	default <R> Parser<R> map(Function<T, R> mapper) {
		Parser<T> self = this;
		return source -> self.parse(source).map(mapper);
	}

	default Parser<WithPos<T>>	withPos(){
		Parser<T> self = this;
		return source -> {
			Position pos = source.getPosition();
			return self.map(v -> new WithPos<>(pos,v))
					.parse(source);
		};
	}




	default Parser<Optional<T>> optional() {
		Parser<T> self = this;
		return source -> {
			ParseResult<T> res = self.parse(source);
			if(res.isSuccess()) {
				return res.map(Optional::ofNullable);
			}
			return ParseResult.success(source, Optional.empty());
		};
	}

	static <R> Parser<R> when(String errorMessage, Predicate<Source> predicate, Parser<R> parse) {
		return source -> {
			if(predicate.test(source)) {
				return parse.parse(source);
			}
			return ParseResult.failure(source, errorMessage);
		};
	}

	@SuppressWarnings("unchecked")
	static <R> Parser<PList<R>> zeroOrMore(Parser<R> parser) {
		return source -> {
			PList res = PList.empty();
			while(source.current() != Source.EOF) {
				ParseResult<R> itemRes = parser.parse(source);
				if(itemRes.isFailure()) {
					return ParseResult.success(source, res);
				}
				res = res.plus(itemRes.getValue());
				if(source.getPosition() == itemRes.getSource().getPosition()){
					break;
				}
				source = itemRes.getSource();
			}
			return ParseResult.success(source,res);
		};
	}

	static <R> Parser<PList<R>> oneOrMore(String errorMessage, Parser<R> parser) {
		return source -> {
			ParseResult<PList<R>> res = zeroOrMore(parser).parse(source);
			if(res.isFailure()) {
				return res;
			}
			PList<R> list = res.getValue();
			if(list.isEmpty()) {
				return ParseResult.failure(source, errorMessage);
			}
			return res;
		};
	}

	static <R> Parser<PList<R>> zeroOrMoreSep(Parser<R> parser, Parser<?> separator) {
		return
			parser.optional().map(opt -> opt.map(v -> PList.val(v)).orElse(PList.empty()))
				  .andThen(zeroOrMore(separator.skipAndThen(parser)))
				  .map(t -> t._1.plusAll(t._2));
	}

	static <R> Parser<PList<R>> oneOrMoreSep(Parser<R> parser, Parser<?> separator) {
		return parser
				.andThen(zeroOrMore(separator.skipAndThen(parser)))
				.map(t -> PList.val(t._1).plusAll(t._2));
	}


	static <R> Parser<R> or(Parser<R>... others) {
		return source -> {

			ParseResult<R> longestResult = null;
			for(Parser<R> other : others) {
				ParseResult<R> otherResult = other.parse(source);
				if(otherResult.isSuccess()) {
					return ParseResult.success(otherResult.getSource(), otherResult.getValue());
				}
				if(longestResult != null) {
					Position pos1 = longestResult.getSource().getPosition();
					Position pos2 = otherResult.getSource().getPosition();
					if(pos2.compareTo(pos1) > 0) {
						longestResult = otherResult;
					}
				}
				else {
					longestResult = otherResult;
				}
			}


			if(longestResult == null) {
				longestResult = ParseResult.failure(source, "No parsers defined for Or parser");
			}
			return longestResult;
		};
	}

	static <R> Parser<R> toDo(String message) {
		return source -> ParseResult.failure(source, "TODO: " + message);
	}

	static Parser<OK> not(String errorMessage, Parser<?> parserNot) {
		return source -> {
			ParseResult<?> res = parserNot.parse(source);
			if(res.isFailure()) {
				return ParseResult.success(source, OK.inst);
			}
			return ParseResult.failure(source, errorMessage);
		};
	}

	default Parser<T> verify(String errorMessage, Predicate<T> predicate) {
		Parser<T> self = this;
		return source -> {
			ParseResult<T> result = self.parse(source);
			if(result.isFailure()) {
				return result;
			}
			if(predicate.test(result.getValue())) {
				return result;
			}
			return ParseResult.failure(source, errorMessage);
		};
	}
}
