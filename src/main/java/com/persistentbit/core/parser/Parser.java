package com.persistentbit.core.parser;

import com.persistentbit.core.OK;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.StrPos;

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
	default <U> Parser<U> skipAnd(Parser<U> nextParser) {
		return and(Objects.requireNonNull(nextParser)).map(t -> t._2);
	}



	default Parser<T> skip(Parser<?> skip) {
		return and(skip).map(t -> t._1);
	}





	default Parser<T> onErrorAddMessage(String errorMessage) {
		Parser<T> self = this;
		return source -> {
			ParseResult<T> res = self.parse(source);
			res = res.onErrorAdd(errorMessage);
			return res;
		};
	}

	default Parser<T> andEof() {
		return skip(Scan.eof);
	}


	default <U> Parser<Tuple2<T, U>> and(Parser<U> nextParser) {
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

	default Parser<T> flatMap(Function<T, Parser<T>> mapper) {
		Parser<T> self = this;
		return source -> {
			ParseResult<T> res = self.parse(source);
			if(res.isFailure()) {
				return res;
			}
			return mapper.apply(res.getValue()).parse(res.getSource());
		};
	}

	default Parser<T> parseThisOrFollowedBy(Function<T, Parser<T>> nextParserSupplier) {
		Parser<T> self = this;
		return source -> {
			ParseResult<T> resThis = self.parse(source);
			if(resThis.isFailure()) {
				return resThis;
			}
			Parser<T>      nextParser  = nextParserSupplier.apply(resThis.getValue());
			ParseResult<T> resFollowed = nextParser.parse(resThis.getSource());
			if(resFollowed.isFailure()) {
				return resThis;
			}
			return resFollowed;
		};
	}


	default <R> Parser<R> mapResult(Function<ParseResult<T>, ParseResult<R>> mapper) {
		Parser<T> self = this;
		return source -> {
			ParseResult<T> res = self.parse(source);
			return mapper.apply(res);
		};
	}

	default Parser<WithPos<T>>	withPos(){
		Parser<T> self = this;
		return source -> {
			StrPos pos = source.position;
			return self.map(v -> new WithPos<>(pos,v))
					.parse(source);
		};
	}

	default Parser<StrPos> onlyPos() {
		Parser<T> self = this;
		return source -> {
			StrPos pos = source.position;
			return self.map(v -> pos).parse(source);
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
			PList  res       = PList.empty();
			Source orgSource = source;
			while(source.current != Source.EOF) {
				ParseResult<R> itemRes = parser.parse(source);
				if(itemRes.isFailure()) {
					return ParseResult.success(source, res);
				}
				res = res.plus(itemRes.getValue());
				if(source.position.equals(itemRes.getSource().position)) {
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
		/*return
			parser.optional().map(opt -> opt.map(v -> PList.val(v)).orElse(PList.empty()))
				  .and(zeroOrMore(separator.skipAnd(parser)))
				  .map(t -> t._1.plusAll(t._2));*/
		return source -> {
			PList<R> res = PList.empty();
			while(source.current != Source.EOF) {
				ParseResult<R> itemRes = parser.parse(source);
				if(itemRes.isFailure()) {
					return ParseResult.success(source, res);
				}
				if(source.position.equals(itemRes.getSource().position)) {
					break;
				}
				res = res.plus(itemRes.getValue());
				source = itemRes.getSource();
				ParseResult sep = separator.parse(source);
				if(sep.isFailure()) {
					return ParseResult.success(source, res);
				}
				if(source.position.equals(sep.getSource().position)) {
					break;
				}
				source = sep.getSource();

			}
			return ParseResult.success(source, res);
		};
	}

	static <R> Parser<PList<R>> oneOrMoreSep(Parser<R> parser, Parser<?> separator) {
		return zeroOrMoreSep(parser, separator).verify("Expected at least 1 item", p -> p.isEmpty() == false);
		/*return parser
			.and(zeroOrMore(separator.skipAnd(parser)))
			.map(t -> PList.val(t._1).plusAll(t._2));*/
	}

	default Parser<T> or(Parser other) {
		return orOf(this, other);
	}


	static <R> Parser<R> orOf(Parser<R>... others) {
		for(int t=0; t<others.length; t++){
			if(others[t] == null){
				throw new RuntimeException("Parser at " + t + " is null");
			}
		}
		return source -> {

			ParseResult<R> longestResult = null;
			for(Parser<R> other : others) {
				ParseResult<R> otherResult = other.parse(source);
				if(otherResult.isSuccess()) {
					return ParseResult.success(otherResult.getSource(), otherResult.getValue());
				}
				if(longestResult != null) {
					StrPos pos1 = longestResult.getSource().position;
					StrPos pos2 = otherResult.getSource().position;
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
