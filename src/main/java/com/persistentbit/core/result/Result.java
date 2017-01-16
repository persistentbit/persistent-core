package com.persistentbit.core.result;

import com.persistentbit.core.collections.PStream;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A Result represents the result of a function.<br>
 * It can be a {@link Success} with a result value.<br>
 * It can be a {@link Failure} with a runtime exception.<br>
 * It can be an {@link Empty} representing an empty value
 *
 * @author Peter Muys
 * @since 27/12/2016
 */
public abstract class Result<T> implements Iterable<T>, Serializable, LoggedValue<Result<T>>{

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
			} catch(Exception e) {
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
	 * Map the Success value or return a Failure or a Empty.<br>
	 *
	 * @param mapper the value mapper
	 * @param <U>    The resulting value type
	 *
	 * @return a new Result
	 */
	public abstract <U> Result<U> map(Function<T, U> mapper);

	/**
	 * Flatmap the Success value or return a Failure or a Empty
	 *
	 * @param mapper the value mapper
	 * @param <U>    The resulting value type
	 *
	 * @return a new Result
	 */
	public abstract <U> Result<U> flatMap(Function<T, Result<U>> mapper);

	/**
	 * Convert this result to an optional.<br>
	 *
	 * @return Some value for success or an empty optional
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
	 * @return The mapped failure or this if it not a failure
	 */
	public abstract Result<T> flatMapFailure(Function<? super Failure<T>, Result<T>> mapper);

	/**
	 * FlatMap this result if it is an {@link Empty}
	 *
	 * @param mapper Empty mapper
	 *
	 * @return The mapped Empty or this if it not an Empty
	 */
	public abstract Result<T> flatMapEmpty(Function<? super Empty<T>, Result<T>> mapper);






	public boolean isPresent() {
		return getOpt().isPresent();
	}

	public abstract boolean isEmpty();

	public boolean isError() {
		return !isPresent() && !isEmpty();
	}

	public abstract Result<T> mapError(Function<Throwable, ? extends Throwable> mapper);

	public Result<T> verify(Predicate<T> verification) {
		return verify(verification, v -> new IllegalStateException("Verification for " + v + " failed!"));
	}

	public Result<T> verify(Predicate<T> verification, String errorMessage) {
		return verify(verification, v -> new IllegalStateException("Verification for " + v + " failed:" + errorMessage));
	}


	public abstract <E extends Throwable> Result<T> verify(Predicate<T> verification,
														   Function<T, E> failureExceptionSupplier
	);

	public abstract Result<String> forEachOrErrorMsg(Consumer<? super T> effect);

	public abstract Result<Throwable> forEachOrException(Consumer<? super T> effect);

	public abstract Result<T> mapLog(Function<LogEntry, LogEntry> mapper);

	public abstract Result<T> withLogs(Consumer<LogEntry> effect);


	public <U> Result<Tuple2<T, U>> combine(Result<U> otherResult) {
		Result<Tuple2<T, U>> result = flatMap(thisValue -> {
			return otherResult.flatMap(otherValue -> {
				return Result.success(Tuple2.of(thisValue, otherValue));
			});
		});
		return result;
	}

	public <B, C> Result<Tuple3<T, B, C>> combine(Result<B> rb, Result<C> rc) {
		return combine(rb).combine(rc)
			.map(t -> Tuple3.of(t._1._1, t._1._2, t._2));
	}

	public <B, C, D> Result<Tuple4<T, B, C, D>> combine(Result<B> rb, Result<C> rc, Result<D> rd) {
		return combine(rb, rc).combine(rd).map(t -> Tuple4.of(t._1._1, t._1._2, t._1._3, t._2));
	}


	/**
	 * Get the Success value or the supplied else value on error or empty
	 *
	 * @param elseValue The return value when there is not Success value
	 *
	 * @return Success value or elseValue
	 */
	public T orElse(T elseValue) {
		return getOpt().orElse(elseValue);
	}

	/**
	 * Get the Success value or the supplied else generated value  on error or empty
	 *
	 * @param elseValueSupplier Supplier for the return value when there is not Success value
	 *
	 * @return Success value or elseValue
	 */
	public T orElseGet(Supplier<T> elseValueSupplier) {
		return getOpt().orElseGet(elseValueSupplier);
	}

	/**
	 * Get the Success value or throw an Exception
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
	 * Get the Success value or throw a RuntimeException.<br>
	 *
	 * @return The value on Success
	 */
	public abstract T orElseThrow();

	@Override
	public Iterator<T> iterator() {
		return getOpt().map(v -> Collections.singletonList(v).iterator()).orElseGet(Collections::emptyIterator);
	}


	/**
	 * When we have a Success value, filter the value, else just return empty or failure
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
	 * Create an Empty result
	 *
	 * @param <U> The normal value type
	 *
	 * @return an Empty
	 */
	@SuppressWarnings("unchecked")
	public static <U> Empty<U> empty() {
		return empty("Empty value");
	}

	public static <U> Empty<U> empty(String message) {
		return new Empty<>(new RuntimeException(message), LogEntryEmpty.inst);
	}

	public static <U> Empty<U> empty(Throwable cause) {
		return new Empty<>(cause, LogEntryEmpty.inst);
	}

	public static <U> Result<U> fromOpt(Optional<U> optValue) {
		if(optValue == null) {
			return Result.failure("optValue is null");
		}
		return Result.result(optValue.orElse(null));
	}

	/**
	 * Create a Success or Empty result
	 *
	 * @param value The Nullable value
	 * @param <U>   The type of the value
	 *
	 * @return An Empty result if value is null or else a Success result
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
	 *
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

	public static <R> Result<R> noExceptions(Callable<R> code) {
		return Result.function().code(l -> {
			try {
				return result(code.call());
			} catch(Exception e) {
				return failure(e);
			}
		});
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


	public abstract Result<T> cleanLogsOnPresent();



	public static <T> Result<PStream<T>> fromSequence(PStream<Result<T>> stream) {
		Optional<Result<T>> optWrong = stream.find(Result::isError);

		if(optWrong.isPresent()) {
			return optWrong.get()
				.flatMapFailure(f -> Result.failure(
					new RuntimeException("sequence contains failure", f.getException()))
				).flatMap(t -> Result.failure("Should not happen"));
		}

		return Result.success(stream.lazy()
								  .filter(r -> r.isEmpty() == false)
								  .map(Result::orElseThrow));

	}
}
