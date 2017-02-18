package com.persistentbit.core.experiments.parser;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.tuples.Tuple2;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
		return andThen(Objects.requireNonNull(nextParser)).map(t -> t._2);
	}
	default Parser<T> andThenSkip(Parser<?> nextParser){
		return andThen(nextParser).map(t -> t._1);
	}

	default Parser<T> skipWhiteSpace() {
		return Scan.whiteSpace.skipAndThen(this);
	}







	default Parser<T> andThenEof() {
		return andThenSkip(Scan.eof);
	}

	static <R> Parser<R> lazy(Supplier<Parser<R>> supplier){
		return source -> supplier.get().parse(source);
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
		return parser;
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
	static <R> Parser<R> when(String errorMessage, Predicate<ParseSource> predicate, Parser<R> parse){
		return source -> {
			source = source.withSnapshot();
			if(predicate.test(source)){
				return parse.parse(source.getSnapshot());
			}
			return ParseResult.failure(source.getSnapshot(),errorMessage);
		};
	}

	static <R> Parser<PList<R>> zeroOrMore(Parser<R> parser){
		return source -> {
			PList res = PList.empty();
			while(true){
				ParseResult<R> itemRes = parser.parse(source.withSnapshot());
				if(itemRes.isFailure()){
					return ParseResult.success(source.getSnapshot(),res);
				}
				source = source.resolved();
			}
		};
	}
	static <R> Parser<PList<R>> oneOrMore(String errorMessage,Parser<R> parser){
		return source -> {
			ParseResult<PList<R>> res = zeroOrMore(parser).parse(source);
			if(res.isFailure()){
				return res;
			}
			PList<R> list = res.getValue();
			if(list.isEmpty()){
				return ParseResult.failure(res.getSource(),errorMessage);
			}
			return res;
		};
	}

	static <R> Parser<PList<R>> zeroOrMoreSep(Parser<R> parser,Parser<?> separator){
		return parser.optional().map(opt -> opt.map(v -> PList.val(v)).orElse(PList.empty()))
			.andThen(zeroOrMore(separator.skipAndThen(parser)))
			.map(t -> t._1.plusAll(t._2));
	}

	static <R> Parser<PList<R>> oneOrMoreSep(Parser<R> parser, Parser<?> separator){
		return parser
			.andThen(zeroOrMore(separator.skipAndThen(parser)))
			.map(t -> PList.val(t._1).plusAll(t._2));
	}

	/*default Parser<T> or(String errorMessage,Parser<T> ... others){
		Parser<T> self = this;
		return source -> {

			ParseResult<T> thisResult = self.parse(source.withSnapshot());
			if(thisResult.isSuccess()){
				return ParseResult.success(thisResult.getSource().resolved(),thisResult.getValue());
			}
			source = thisResult.getSource().getSnapshot();
			for(Parser<T> other : others){
				ParseResult<T> otherResult = other.parse(source.withSnapshot());
				if(otherResult.isSuccess()) {
					return ParseResult.success(otherResult.getSource().resolved(), otherResult.getValue());
				}
				source = otherResult.getSource().getSnapshot();
			}

			return ParseResult.failure(source, errorMessage);
		};
	}*/
	static <R> Parser<R> or(String errorMessage, Parser<R>... others){
		return source -> {
			source = source.withSnapshot();
			for(Parser<R> other : others){
				ParseResult<R> otherResult = other.parse(source);
				if(otherResult.isSuccess()) {
					return ParseResult.success(otherResult.getSource().resolved(), otherResult.getValue());
				}
				source = otherResult.getSource().getSnapshot();
			}
			return ParseResult.failure(source, errorMessage);
		};
	}
}
