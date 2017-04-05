package com.persistentbit.core.patternmatching;

import com.persistentbit.core.collections.LList;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A Matcher is a switch/case with {@link Predicate}s as matcher.<br>
 *
 * @author petermuys
 * @since 5/04/17
 */
public class Matcher<V,R> implements Function<V,R>{

	/**
	 * Represent a mapper function that is executed when a case is matched.<br>
	 * The signature is rootMatcher -> V -> R
	 * @param <V> The value that is matched
	 * @param <R> The result value
	 */
	public interface MatchConverter<M,V,R> extends Function<Matcher<M,R>,Function<V, R>>{}

	private final Predicate<V> match;
	private final MatchConverter<V,V,R> convert;
	private final LList<Matcher<V,R>> matchers;


	private Matcher(Predicate<V> match, MatchConverter<V,V,R> convert, LList<Matcher<V, R>> matchers
	) {
		this.match = match;
		this.convert = convert;
		this.matchers = matchers;
	}

	/**
	 * Creates a default case matcher.<br>
	 * Every Matcher starts from this<br>
	 * @param mapper The Default case mapper
	 * @param <V> The match value type
	 * @param <R> The resulting type
	 * @return A new Matcher
	 */
	public static <V extends Object,R> Matcher<V,R> defaultCase(Function<V,R> mapper){
		return new Matcher<>(v -> true, m -> mapper);
	}

	/**
	 * Creates a default case matcher that throws an {@link IllegalArgumentException}
	 * @param <V>
	 * @param <R>
	 * @return
	 */
	public static <V extends Object,R> Matcher<V,R> defaultCaseThrowException() {
		return defaultCase(v -> {
			throw new IllegalStateException("No matcher for " + v);
		});
	}

	private Matcher(Predicate<V> match, MatchConverter<V,V,R> convert){
		this(match,convert,LList.empty());
	}

	boolean matches(V value){
		return match.test(value);
	}


	@Override
	public R apply(V v) {
		for(Matcher<V,R> matcher : matchers){
			if(matcher.match.test(v)){
				return matcher.convert.apply(this).apply(v);
			}
		}
		return convert.apply(this).apply(v);
	}

	/**
	 * Wrapper for apply
	 * @param v The value to match
	 * @return The converted value
	 */
	public R match(V v){
		return apply(v);
	}


	/**
	 * Add a new match case
	 * @param match The matcher for this case
	 * @param convert The converter if this case matches
	 * @return The New Matcher
	 */
	public Matcher<V,R>	addCase(Predicate<V> match, MatchConverter<V,V,R> convert){
		return new Matcher<>(
			this.match,
			this.convert,
			matchers.prepend(new Matcher<>(match,convert))
		);
	}

	public <C> Matcher<V,R> caseClassIs(Class<C> cls, MatchConverter<V,C,R> convert){
		return addCase(UMatch.classIs(cls),m -> v -> convert.apply(m).apply((C)v));
	}
	public <C> Matcher<V,R> caseIsAssignableTo(Class<C> cls, MatchConverter<V,C,R> convert){
		return addCase(UMatch.isAssignableTo(cls),m -> v ->
			convert
				.apply(m)
				.apply((C)v )
		);
	}
}
