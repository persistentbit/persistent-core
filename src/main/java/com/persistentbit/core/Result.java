package com.persistentbit.core;

import com.persistentbit.core.logging.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
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


	/*

	static public class FLogging extends FunctionLogging{

		public FLogging(LogEntryFunction lef) {
			super(lef, 2);
		}

		public <R> Result<R> logNoResultLog(FunctionLogging.LoggedFunction<Result<R>> code) {
			StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
			LogEntryFunction  fe  = LogEntryFunction.of(new LogContext(ste));

			try {
				Result<R> result = code.run(this);
				this.functionDoneTimestamp(System.currentTimeMillis());
				return result.mapLog(this.getLog());
			} catch(Exception e) {
				throw new LoggedException(e, this.getLog());
			}
		}

		public <R> Result<R> log(FunctionLogging.LoggedFunction<Result<R>> code) {
			StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
			LogEntryFunction  fe  = LogEntryFunction.of(new LogContext(ste));
			try {
				Result<R> result = code.run(this);
				this.functionDoneTimestamp(System.currentTimeMillis());
				this.functionResult(result);
				return result.mapLog(this.getLog());
			} catch(Exception e) {
				throw new LoggedException(e, this.getLog());
			}
		}
	}

	public static Logged.FLogging function() {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		LogEntryFunction  fe  = LogEntryFunction.of(new LogContext(ste));
		return new Logged.FLogging(fe);
	}

	public static Logged.FLogging function(Object p1, Object... params) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		LogEntryFunction  fe  = LogEntryFunction.of(new LogContext(ste));
		Logged.FLogging   res = new Logged.FLogging(fe);
		res.params(params);
		return new Logged.FLogging(fe);
	}*/


	/**
	 * Map the Success value or return a Failure or a Empty
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
	 * Convert this result to an optional
	 *
	 * @return Some value for success or an empty optional
	 */
	public abstract Optional<T> getOpt();


	public Result<T> ifPresent(Consumer<T> effect){
		getOpt().ifPresent(effect);
		return this;
	}


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
	 * @param r The code to run
	 */
	public abstract void ifEmpty(Runnable r);


	/**
	 * Run code if this is a Failure result
	 *
	 * @param e The Failure exception
	 */
	public abstract void ifFailure(Consumer<Throwable> e);

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
		return new Empty<>(new RuntimeException(message),LogEntryEmpty.inst);
	}

	public static <U> Empty<U> empty(Throwable cause) {
		return new Empty<>(cause,LogEntryEmpty.inst);
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
		return new Failure<U>(exception, LogEntryEmpty.inst);
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
	public static <T, R> Function<T, Result<R>> toResult(Function<T, R> f) {
		return x -> {
			try {
				return result(f.apply(x));
			} catch(RuntimeException e) {
				return failure(e);
			} catch(Exception e) {
				return failure(new RuntimeException(e));
			}
		};

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
	 * A SUCCESS RESULT
	 *
	 * @param <T> Result Type
	 */
	public static class Success<T> extends Result<T>{

		private final LogEntry log;
		private final T        value;

		private Success(T value, LogEntry log) {
			this.value = Objects.requireNonNull(value, "Success value is null, use an Empty value instead");
			this.log = Objects.requireNonNull(log, "Log must be non null");
		}



		private Success(T value) {
			this(value, LogEntryEmpty.inst);
		}

		@Override
		public Result<T> mapLog(Function<LogEntry, LogEntry> mapper) {
			return new Success<>(value,mapper.apply(log));
		}

		@Override
		public <U> Result<U> map(Function<T, U> mapper) {
			return success(mapper.apply(value));
		}


		@Override
		public LogEntry getLog() {
			return log;
		}

		@Override
		public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
			return mapper.apply(value);
		}

		@Override
		public Optional<T> getOpt() {
			return Optional.ofNullable(value);
		}

		@Override
		public T orElseThrow() {
			return value;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public Result<T> mapError(Function<Throwable, ? extends Throwable> mapper) {
			return this;
		}

		@Override
		public String toString() {
			return "Success(" + value + ")";
		}

		@Override
		public <E extends Throwable> Result<T> verify(Predicate<T> verification,
													  Function<T, E> failureExceptionSupplier
		) {
			return verification.test(value)
				? this
				: Result.failure(failureExceptionSupplier.apply(value));
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			Success<?> success = (Success<?>) o;

			return value.equals(success.value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public void ifEmpty(Runnable r) {

		}


		@Override
		public void ifFailure(Consumer<Throwable> e) {

		}


		@Override
		public Result<T> filter(Predicate<T> filter) {
			return filter.test(value)
				? this
				: empty();
		}

		@Override
		public Result<String> forEachOrErrorMsg(Consumer<? super T> effect) {
			effect.accept(value);
			return empty();
		}

		@Override
		public Result<Throwable> forEachOrException(Consumer<? super T> effect) {
			effect.accept(value);
			return empty();
		}
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
	 * An EMPTY result.
	 *
	 * @param <T> The non-empty result type.
	 */
	public static class Empty<T> extends Result<T>{

		private Throwable exception;
		private LogEntry  log;

		public Empty(Throwable e, LogEntry log) {
			this.exception = Objects.requireNonNull(e);
			this.log = Objects.requireNonNull(log);

		}

		@Override
		public Result<T> mapLog(Function<LogEntry, LogEntry> mapper) {
			return new Empty<>(exception,mapper.apply(log));
		}

		@Override
		public LogEntry getLog() {
			return log;
		}

		@Override
		public <U> Empty<U> map(Function<T, U> mapper) {
			return new Empty<>(exception, log);
		}

		@Override
		public <U> Empty<U> flatMap(Function<T, Result<U>> mapper) {
			return new Empty<>(exception, log);
		}

		@Override
		public Optional<T> getOpt() {
			return Optional.empty();
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public T orElseThrow() {
			throw new EmptyException(new LoggedException(exception, log));
		}

		@Override
		public String toString() {
			return "Empty()";
		}

		@Override
		public int hashCode() {
			return 1;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Empty;
		}

		@Override
		public void ifEmpty(Runnable r) {
			r.run();
		}


		@Override
		public void ifFailure(Consumer<Throwable> e) {

		}

		@Override
		public Result<T> filter(Predicate<T> filter) {
			return this;
		}

		@Override
		public Result<T> mapError(Function<Throwable, ? extends Throwable> mapper) {
			return this;
		}

		@Override
		public <E extends Throwable> Result<T> verify(Predicate<T> verification,
													  Function<T, E> failureExceptionSupplier
		) {
			return this;
		}

		@Override
		public Result<String> forEachOrErrorMsg(Consumer<? super T> effect) {
			return empty();
		}

		@Override
		public Result<Throwable> forEachOrException(Consumer<? super T> effect) {
			return empty();
		}
	}

	/**
	 * Exception thrown when we try to get a value from a {@link Failure}.
	 */
	public static class FailureException extends RuntimeException{

		public FailureException(Throwable failureCause) {
			super("Can't get value from a Failure Result", failureCause);
		}
	}

	public static class Failure<T> extends Result<T>{

		private final Throwable exception;
		private final LogEntry  log;


		public Failure(Throwable exception, LogEntry log) {

			this.exception = Objects.requireNonNull(exception);
			this.log = Objects.requireNonNull(log);
		}


		public Failure(String error, LogEntry log) {
			this(new RuntimeException(error), log);
		}


		@Override
		public Failure<T> mapLog(Function<LogEntry, LogEntry> mapper) {
			return new Failure<>(exception,mapper.apply(log));
		}

		@Override
		public LogEntry getLog() {
			return log;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <U> Result<U> map(Function<T, U> mapper) {
			return new Failure<>(exception, log);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
			return new Failure<>(exception, log);
		}

		@Override
		public Optional<T> getOpt() {
			return Optional.empty();
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		public T orElseThrow() {
			if(log.isEmpty()){
				throw new FailureException(exception);
			}
			throw new FailureException(new LoggedException(exception, log));
		}

		@Override
		public void ifEmpty(Runnable r) {

		}



		@Override
		public void ifFailure(Consumer<Throwable> e) {
			e.accept(exception);
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			Failure<?> failure = (Failure<?>) o;

			return exception.equals(failure.exception);
		}

		@Override
		public int hashCode() {
			return exception.hashCode();
		}

		@Override
		public String toString() {
			return "Failure(" + exception.getMessage() + ")";
		}

		@Override
		public Result<T> filter(Predicate<T> filter) {
			return this;
		}

		@Override
		public Result<T> mapError(Function<Throwable, ? extends Throwable> mapper) {
			return Result.failure(mapper.apply(exception));
		}

		@Override
		public <E extends Throwable> Result<T> verify(Predicate<T> verification,
													  Function<T, E> failureExceptionSupplier
		) {
			return this;
		}

		@Override
		public Result<String> forEachOrErrorMsg(Consumer<? super T> effect) {
			return Result.success(exception.getMessage());
		}

		@Override
		public Result<Throwable> forEachOrException(Consumer<? super T> effect) {
			return Result.success(exception);
		}
	}
}
