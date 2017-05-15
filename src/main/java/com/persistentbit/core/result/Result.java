package com.persistentbit.core.result;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.doc.annotations.DInfo;
import com.persistentbit.core.logging.FunctionLogging;
import com.persistentbit.core.logging.LoggedException;
import com.persistentbit.core.logging.LoggedValue;
import com.persistentbit.core.logging.entries.LogContext;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.logging.entries.LogEntryEmpty;
import com.persistentbit.core.logging.entries.LogEntryFunction;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.tuples.Tuple3;
import com.persistentbit.core.tuples.Tuple4;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * A Result represents the result of a function.<br>
 * It can be a {@link Success} with a result value.<br>
 * It can be a {@link Failure} with a runtime exception.<br>
 * It can be an {@link Empty} representing an empty value
 *
 * @author Peter Muys
 * @since 27/12/2016
 */
@DInfo("Represents the result of a function call")
public abstract class Result<T> implements Serializable, LoggedValue<Result<T>>{

	public static <T> Failure<T> TODO() {
		return Result.failure("TODO");
	}

	@DInfo("Logging in a function")
	public static class FLogging extends FunctionLogging{

		public FLogging(LogEntryFunction lef, int stackEntryIndex) {
			super(lef, stackEntryIndex);
		}
		public FLogging(LogEntryFunction lef) {
			this(lef, 2);
		}

		public <R> Result<R> codeNoResultLog(FunctionLogging.LoggedFunction<Result<R>> code) {
			try {
				Result<R> result = code.run(this);
				functionDoneTimestamp(System.currentTimeMillis());
				return result;
			} catch(LoggedException le) {
				return Result.failure(le.setLogs(entry.append(le.getLogs())));
			} catch(Exception e) {
				return Result.failure(e);
			}
		}

		public FLogging withThis(Object thisContainer) {
			entry = entry.withThis(thisContainer == null ? "null" : thisContainer.toString());
			return this;
		}

		@SuppressWarnings("unchecked")
		public <R> Result<R> code(FunctionLogging.LoggedFunction<Result<R>> code) {
			try {
				Result<R> result = code.run(this);
				functionDoneTimestamp(System.currentTimeMillis());
				functionResult(result);
				return result.mapLog(resultLog ->
										 entry.append(resultLog)
				);
			} catch(LoggedException le) {
				return Result.failure(le.setLogs(entry.append(le.getLogs())));
			} catch(Throwable e) {
				return Result.failure(new LoggedException(e, entry));
			}
		}

	}


	public static FLogging function() {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		LogEntryFunction  fe  = LogEntryFunction.of(new LogContext(ste));
		return new FLogging(fe);
	}

	public static FLogging function(Object... params) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		LogEntryFunction  fe  = LogEntryFunction.of(new LogContext(ste));
		FLogging          res = new FLogging(fe);
		res.params(params);
		return res;
	}


	/**
	 * Map the Success value orOf return a Failure orOf a Empty.<br>
	 *
	 * @param mapper the value mapper
	 * @param <U>    The resulting value type
	 *
	 * @return a new Result
	 */
	public abstract <U> Result<U> map(Function<T, U> mapper);

	/**
	 * Flatmap the Success value orOf return a Failure orOf a Empty.<br>
	 * The new Result's LogEntry is this {@link LogEntry} with the mapped appended.<br>
	 * @param mapper the value mapper
	 * @param <U>    The resulting value type
	 *
	 * @return a new Result
	 */
	public abstract <U> Result<U> flatMap(Function<T, Result<U>> mapper);


	public Result<T> flatMapIfTrue(Predicate<T> isTrue, Function<T, Result<T>> mapper) {
		return flatMap(value -> {
			if(isTrue.test(value)) {
				return mapper.apply(value);
			}
			return Result.success(value);
		});
	}

	public Result<T> mapIfTrue(Predicate<T> isTrue, Function<T, T> mapper) {
		return map(value -> {
			if(isTrue.test(value)) {
				return mapper.apply(value);
			}
			return value;
		});
	}

	public Result<T> throwOnError(){
		if(isError()){
			orElseThrow();
		}
		return this;
	}

	/**
	 * Convert this result to an optional.<br>
	 *
	 * @return Some value for success orOf an empty optional
	 */
	public abstract Optional<T> getOpt();

	/**
	 * Check if we have a real instance of a result.<br>
	 * Lazy Results returns false when the value is not yet calculated.<br>
	 * Async Results return false when the value is not yet returned from the async call.<br>
	 * @return true if we have a Result.
	 * @see ResultLazy
	 * @see ResultAsync
	 * @see #completed()
	 */
	public boolean isComplete() {
		return true;
	}

	/**
	 * Wait for this result to complete.<br>
	 * For lazy results, calculate the value, for async result with
	 * for the async result to be completed.<br>
	 * @return The completed result.
	 * @see #isComplete()
	 */
	public Result<T> completed() {
		return this;
	}

	/**
	 * FlatMap this result if it is a {@link Failure}
	 *
	 * @param mapper Failure mapper
	 *
	 * @return The mapped failure orOf this if it not a failure
	 */
	public abstract Result<T> flatMapFailure(Function<? super Failure<T>, Result<T>> mapper);

	public abstract Optional<Throwable> getEmptyOrFailureException();

	/**
	 * FlatMap this result if it is an {@link Empty}
	 *
	 * @param mapper Empty mapper
	 *
	 * @return The mapped Empty orOf this if it not an Empty
	 */
	public abstract Result<T> flatMapEmpty(Function<? super Empty<T>, Result<T>> mapper);

	/**
	 * If we have an {@link Empty} result, map the empty to a value.
	 * @param mapper The Mapper
	 * @return The new Result
	 */
	public Result<T> mapEmpty(Function<? super Empty<T>, T> mapper){
		return flatMapEmpty(e -> {
			T value = mapper.apply(e);
			return Result.result(value);
		});
	}



	/**
	 * FlatMap this result if it is an {@link Empty} orOf a {@link Failure}
	 *
	 * @param mapper Empty/Failure mapper
	 *
	 * @return The mapped Empty/Failer orOf this if it a Success
	 */
	public abstract Result<T> flatMapNoSuccess(BiFunction<Result<T>, Throwable, Result<T>> mapper);


	/**
	 * Is this a {@link Success} value?
	 * @return true if we have a result value.
	 */
	public boolean isPresent() {
		return getOpt().isPresent();
	}

	/**
	 * is this an {@link Empty} value?
	 * @return true if empty
	 */
	public abstract boolean isEmpty();

	/**
	 * Is this a {@link Failure} value ?
	 * @return true if failure
	 */
	public boolean isError() {
		return !isPresent() && !isEmpty();
	}

	/**
	 * If this is a {@link Failure}, map the causing Exception.
	 * @param mapper The Exception mapper
	 * @return A failure with the new Exception
	 */
	public abstract Result<T> mapError(Function<Throwable, ? extends Throwable> mapper);


	/**
	 * If this is a {@link Success}, verify the value
	 * @param verification The verification Predicate
	 * @return The Verified {@link Success} orOf a {@link Failure}
	 */
	public Result<T> verify(Predicate<T> verification) {
		return verify(verification, v -> new IllegalStateException("Verification for " + v + " failed!"));
	}

	/**
	 * If this is a {@link Success}, verify the value
	 * @param verification The verification Predicate
	 * @param errorMessage The Exception Error message
	 * @return The Verified {@link Success} orOf a {@link Failure}
	 */
	public Result<T> verify(Predicate<T> verification, String errorMessage) {
		return verify(verification, v -> new IllegalStateException("Verification for " + v + " failed:" + errorMessage));
	}




	/**
	 * If this is a {@link Success}, verify the value
	 * @param verification The verification Predicate
	 * @param failureExceptionSupplier A supplier for a Failure Exception incase the verification returns false
	 * @param <E> The Exception type
	 * @return The Verified {@link Success} orOf a {@link Failure}
	 */
	public abstract <E extends Throwable> Result<T> verify(Predicate<T> verification,
														   Function<T, E> failureExceptionSupplier
	);

	public abstract Result<String> forEachOrErrorMsg(Consumer<? super T> effect);

	public abstract Result<Throwable> forEachOrException(Consumer<? super T> effect);

	/**
	 * Returns a new {@link Result} with the Logs mapped
	 * @param mapper The Log mapper
	 * @return a new Result
	 */
	public abstract Result<T> mapLog(Function<LogEntry, LogEntry> mapper);

	/**
	 * Execute an effect with the Log
	 * @param effect The effect, taking a {@link LogEntry} as input
	 * @return this.
	 */
	public abstract Result<T> withLogs(Consumer<LogEntry> effect);


	/**
	 * Combine this Result with an other Result into a Result with a Tuple2 of this and the other result
	 * @param otherResult The Other result
	 * @param <U> The type of the other result
	 * @return The combined result
	 */
	public <U> Result<Tuple2<T, U>> combine(Result<U> otherResult) {
		return flatMap(thisValue ->
			otherResult.flatMap(otherValue ->
				Result.success(Tuple2.of(thisValue, otherValue))
			)
		);
	}

	/**
	 * Combine this Result with 2 other Results into a Result with a Tuple3.<br>
	 * @param rb other Result
	 * @param rc other Result
	 * @param <B> Type of rb
	 * @param <C> Type of rc
	 * @return a Tuple3 result
	 */
	public <B, C> Result<Tuple3<T, B, C>> combine(Result<B> rb, Result<C> rc) {
		return combine(rb).combine(rc)
			.map(t -> Tuple3.of(t._1._1, t._1._2, t._2));
	}

	/**
	 * Combine this Result with 3 other Results into a Result with a Tuple4.<br>
	 * @param rb other Result
	 * @param rc other Result
	 * @param rd other Result
	 * @param <B> Type of rb
	 * @param <C> Type of rc
	 * @param <D> Type of rd
	 * @return a Tuple4 result
	 */
	public <B, C, D> Result<Tuple4<T, B, C, D>> combine(Result<B> rb, Result<C> rc, Result<D> rd) {
		return combine(rb, rc).combine(rd).map(t -> Tuple4.of(t._1._1, t._1._2, t._1._3, t._2));
	}


	/**
	 * Get the Success value orOf the supplied else value on error orOf empty
	 *
	 * @param elseValue The return value when there is not Success value
	 *
	 * @return Success value orOf elseValue
	 */
	public T orElse(T elseValue) {
		return getOpt().orElse(elseValue);
	}

	/**
	 * Get the Success value orOf the supplied else generated value  on error orOf empty
	 *
	 * @param elseValueSupplier Supplier for the return value when there is not Success value
	 *
	 * @return Success value orOf elseValue
	 */
	public T orElseGet(Supplier<T> elseValueSupplier) {
		return getOpt().orElseGet(elseValueSupplier);
	}

	/**
	 * Get the Success value orOf throw an Exception
	 *
	 * @param exceptionSupplier The Exception supplier
	 * @param <E>               The Exception type
	 *
	 * @return The value on Success
	 *
	 * @throws E Exception thrown when there is no value
	 */
	public <E extends Throwable> T orElseThrow(Supplier<? extends E> exceptionSupplier) throws E {
		return getOpt().orElseThrow(exceptionSupplier);
	}

	/**
	 * Get the Success value orOf throw a RuntimeException.<br>
	 *
	 * @return The value on Success
	 */
	public abstract T orElseThrow();

	/*
	@Override
	public Iterator<T> iterator() {
		return getOpt().map(v -> Collections.singletonList(v).iterator()).orElseGet(Collections::emptyIterator);
	}*/


	/**
	 * When we have a Success value, filter the value, else just return empty orOf failure
	 *
	 * @param filter The filter predicate
	 *
	 * @return The filtered result
	 */
	public abstract Result<T> filter(Predicate<T> filter);


	/**
	 * Run code if this is an Empty result
	 *
	 * @param effect The code to run
	 * @return Same result like this
	 */
	public abstract Result<T> ifEmpty(Consumer<Empty<T>> effect);



	/**
	 * Run code if this is a Failure result
	 *
	 * @param effect The Failure exception
	 * @return Same result like this
	 */
	public abstract Result<T> ifFailure(Consumer<Failure<T>> effect);

	/**
	 * Run code if this is a {@link Success} result
	 * @param effect The effect code taking a {@link Success} as parameter
	 * @return this result
	 */
	public abstract Result<T> ifPresent(Consumer<Success<T>> effect);


	/**
	 * Create a Success result
	 *
	 * @param value The NOT NULLABLE Success value
	 * @param <U>   The Result value type
	 *
	 * @return a Success
	 */
	public static <U> Success<U> success(U value) {
		return new Success<>(value);
	}


	/**
	 * Create an Empty result with a RuntimeException with message "Empty value".<br>
	 *
	 * @param <U> The normal value type
	 *
	 * @return an {@link Empty}
	 */
	@SuppressWarnings("unchecked")
	public static <U> Empty<U> empty() {
		return empty("Empty value");
	}

	/**
	 * Create an Empty result with a RuntimeException with the given message.<br>
	 * @param message The exception message
	 * @param <U> The type of the Empty
	 * @return The empty
	 */
	public static <U> Empty<U> empty(String message) {
		return new Empty<>(new RuntimeException(message), LogEntryEmpty.inst);
	}

	/**
	 * Create an Empty result with a given Throwable cause.<br>
	 * @param cause The cause for the empty
	 * @param <U> The type of Result
	 * @return The new Empty
	 */
	public static <U> Empty<U> empty(Throwable cause) {
		return new Empty<>(cause, LogEntryEmpty.inst);
	}

	public static <U> Result<U> fromOpt(Optional<U> optValue) {
		if(optValue == null) {
			return Result.<U>failure("optValue is null").logFunction("null");
		}
		return Result.result(optValue.orElse(null));
	}

	/**
	 * Create a Success orOf Empty result
	 *
	 * @param value The Nullable value
	 * @param <U>   The type of the value
	 *
	 * @return An Empty result if value is null orOf else a Success result
	 */
	public static <U> Result<U> result(U value) {
		return value == null ? empty() : success(value);
	}

	/**
	 * Create a failure result
	 *
	 * @param error The failure Exception message
	 * @param <U>   The result type
	 *
	 * @return a Failure result
	 */
	public static <U> Failure<U> failure(String error) {
		return new Failure<>(error, LogEntryEmpty.inst);
	}

	/**
	 * Create a failure result
	 *
	 * @param exception The failure RuntimeException
	 * @param <U>       The result type
	 *
	 * @return a Failure result
	 */
	public static <U> Failure<U> failure(Throwable exception) {
		return new Failure<>(exception, LogEntryEmpty.inst);
	}

	public static <U> ResultAsync<U> async(Supplier<Result<U>> code) {
		return ResultAsync.of(code);
	}

	public static <U> Result<U> async(Executor executor, Supplier<Result<U>> code) {
		return ResultAsync.of(executor, code);
	}

	public static <U> ResultLazy<U> lazy(Supplier<Result<U>> lazy) {
		return ResultLazy.of(lazy);
	}


	/**
	 * Create a new Function, returning a Result over the return type of the supplied function.<br>
	 * If an exception is thrown by the function, a {@link Failure} is returned.
	 * @param f   The function to convert
	 * @param <T> The function argument type
	 * @param <R> The Result value type
	 *
	 * @return A new function returning a Result
	 */
	public static <T, R> Function<T, Result<R>> toResultFunction(Function<T, R> f) {
		return x -> {
			try {
				return result(f.apply(x));
			} catch(Exception e) {
				return failure(e);
			}
		};

	}


	/**
	 * Run the supplied function, returning the return value wrapped in a {@link Result}.<br>
	 * An Exception is automatically converted to a {@link Failure} result
	 * @param code The function to run
	 * @param <R> The result type of the function
	 * @return A Result of type R
	 */
	public static <R> Result<R> noExceptions(Callable<R> code) {
		return Result.function().code(l ->
			result(code.call())
		);
	}

	public static <T, U, R> Function<T, Function<U, Result<R>>> higherToResult(Function<T, Function<U, R>> f) {
		return x -> y -> {
			try {
				return result(f.apply(x).apply(y));
			} catch(RuntimeException e) {
				return failure(e);
			} catch(Exception e) {
				return failure(new RuntimeException(e));
			}
		};

	}

	/**
	 * Exception thrown when we try to get a value from an {@link Empty}
	 */
	public static class EmptyException extends RuntimeException{

		public EmptyException() {
			super("Can't get value from an Empty result!");
		}

		public EmptyException(Throwable cause) {
			super("Can't get value from an Empty result!", cause);
		}
	}


	/**
	 * Exception thrown when we try to get a value from a {@link Failure}.
	 */
	public static class FailureException extends RuntimeException{

		public FailureException(Throwable failureCause) {
			super("Can't get value from a Failure Result:" + failureCause.getMessage(), failureCause);
		}
	}


	public abstract <U> U match(
		Function<Success<T>, U> onSuccess,
		Function<Empty<T>, U> onEmpty,
		Function<Failure<T>, U> onFailure
	);



	public abstract Result<T> cleanLogsOnPresent();

	public Result<T>	logFunction(Object...parameters){
		LogContext lc = new LogContext(Thread.currentThread().getStackTrace()[2]);
		LogEntryFunction fun =
				LogEntryFunction.of(lc)
					.withParams(parameters)
					.withResultValue(this.toString())
				;
		return mapLog(l -> fun.append(l));
	}



	public static <T> Result<PStream<T>> fromSequence(PStream<Result<T>> stream) {
		Optional<Result<T>> optWrong = stream.find(Result::isError);

		if(optWrong.isPresent()) {
			return optWrong.get()
				.flatMapFailure(f -> Result.failure(
					new RuntimeException("sequence contains failure", f.getException()))
				).flatMap(t -> Result.failure("Should not happen"));
		}

		return Result.success(stream.lazy()
								  .map(r -> r.orElse(null)));

	}

	public static <T> Result<List<T>> fromSequence(List<Result<T>> list){
		Optional<Result<T>> optWrong =  list.stream().filter(res -> res.isError()).findFirst();
		if(optWrong.isPresent()) {
			return optWrong.get()
					.flatMapFailure(f -> Result.failure(
							new RuntimeException("list contains failure", f.getException()))
					).flatMap(t -> Result.failure("Should not happen"));
		}
		return Result.success(list.stream().map(r -> r.orElse(null)).collect(Collectors.toList()));
	}
}
