package com.persistentbit.core;

import com.persistentbit.core.logging.LogEntry;

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
 * TODOC
 *
 * @author petermuys
 * @since 2/01/17
 */
public abstract class ResultNoLogs<T> implements Iterable<T>, Serializable{

	/**
	 * Map the Success value or return a Failure or a Empty
	 *
	 * @param mapper the value mapper
	 * @param <U>    The resulting value type
	 * @return a new Result
	 */
	public abstract <U> ResultNoLogs<U> map(Function<T, U> mapper);

	/**
	 * Flatmap the Success value or return a Failure or a Empty
	 *
	 * @param mapper the value mapper
	 * @param <U>    The resulting value type
	 * @return a new Result
	 */
	public abstract <U> ResultNoLogs<U> flatMap(Function<T, ResultNoLogs<U>> mapper);

	/**
	 * Convert this result to an optional
	 *
	 * @return Some value for success or an empty optional
	 */
	public abstract Optional<T> getOpt();

	public abstract Optional<LogEntry> getSuccessLog();


	public boolean isPresent() {
		return getOpt().isPresent();
	}

	public abstract boolean isEmpty();

	public boolean isError() {
		return !isPresent() && !isEmpty();
	}

	public abstract ResultNoLogs<T> mapError(Function<Throwable,? extends Throwable> mapper);
	public ResultNoLogs<T> verify(Predicate<T> verification){
		return verify(verification,v -> new IllegalStateException("Verification for " + v + " failed!"));
	}

	public ResultNoLogs<T> verify(Predicate<T> verification,String errorMessage){
		return verify(verification,v -> new IllegalStateException("Verification for " + v + " failed:" + errorMessage));
	}

	public abstract <E extends Throwable> ResultNoLogs<T> verify(Predicate<T> verification, Function<T, E> failureExceptionSupplier);


	public abstract ResultNoLogs<String> forEachOrErrorMsg(Consumer<? super T> effect);

	public abstract ResultNoLogs<Throwable> forEachOrException(Consumer<? super T> effect);


	/**
	 * Get the Success value or the supplied else value on error or empty
	 *
	 * @param elseValue The return value when there is not Success value
	 * @return Success value or elseValue
	 */
	public T orElse(T elseValue) {
		return getOpt().orElse(elseValue);
	}

	/**
	 * Get the Success value or the supplied else generated value  on error or empty
	 *
	 * @param elseValueSupplier Supplier for the return value when there is not Success value
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
	 * @return The value on Success
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
	 * @return The filtered result
	 */
	public abstract ResultNoLogs<T> filter(Predicate<T> filter);


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
	 * @return a Success
	 */
	public static <U> ResultNoLogs.Success<U> success(U value) {
		return new ResultNoLogs.Success<>(value);
	}

	public static <U> ResultNoLogs.SuccessLogged success(U value, LogEntry logEntry){
		return new ResultNoLogs.SuccessLogged<>(value, logEntry);
	}

	static private final ResultNoLogs.Empty theEmpty = new ResultNoLogs.Empty();

	/**
	 * Create an Empty result
	 *
	 * @param <U> The normal value type
	 * @return an Empty
	 */
	@SuppressWarnings("unchecked")
	public static <U> ResultNoLogs.Empty<U> empty() {
		return (ResultNoLogs.Empty<U>) theEmpty;
	}

	/**
	 * Create a Success or Empty result
	 *
	 * @param value The Nullable value
	 * @param <U>   The type of the value
	 * @return An Empty result if value is null or else a Success result
	 */
	public static <U> ResultNoLogs<U> result(U value) {
		return value == null ? empty() : success(value);
	}

	/**
	 * Create a failure result
	 *
	 * @param error The failure Exception message
	 * @param <U>   The result type
	 * @return a Failure result
	 */
	public static <U> ResultNoLogs.Failure<U> failure(String error) {
		return new ResultNoLogs.Failure<>(error);
	}

	/**
	 * Create a failure result
	 *
	 * @param exception The failure RuntimeException
	 * @param <U>       The result type
	 * @return a Failure result
	 */
	public static <U> ResultNoLogs.Failure<U> failure(Throwable exception) {
		return new ResultNoLogs.Failure<>(exception);
	}

	public static <U> ResultNoLogs.Failure<U> failure(String error, Throwable causeException) {
		return failure(new RuntimeException(error,causeException));
	}

	/**
	 * Create a new Function, returning a Result over the return type of the supplied function.<br>
	 *
	 * @param f   The function to convert
	 * @param <T> The function argument type
	 * @param <R> The Result value type
	 * @return A new function returning a Result
	 */
	public static <T, R> Function<T, ResultNoLogs<R>> toResult(Function<T, R> f) {
		return x -> {
			try {
				return result(f.apply(x));
			} catch (RuntimeException e) {
				return failure(e);
			} catch (Exception e) {
				return failure(new RuntimeException(e));
			}
		};

	}

	public static <T, U, R> Function<T, Function<U, ResultNoLogs<R>>> higherToResult(Function<T, Function<U, R>> f) {
		return x -> y -> {
			try {
				return result(f.apply(x).apply(y));
			} catch (RuntimeException e) {
				return failure(e);
			} catch (Exception e) {
				return failure(new RuntimeException(e));
			}
		};

	}

	public static class Success<T> extends ResultNoLogs<T> {
		private final T value;

		private Success(T value) {
			this.value = Objects.requireNonNull(value, "Success value is null, use an Empty value instead");
		}

		@Override
		public <U> ResultNoLogs<U> map(Function<T, U> mapper) {
			return success(mapper.apply(value));
		}

		@Override
		public Optional<LogEntry> getSuccessLog() {
			return Optional.empty();
		}

		@Override
		public <U> ResultNoLogs<U> flatMap(Function<T, ResultNoLogs<U>> mapper) {
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
		public ResultNoLogs<T> mapError(Function<Throwable, ? extends Throwable> mapper) {
			return this;
		}

		@Override
		public String toString() {
			return "Success(" + value + ")";
		}

		@Override
		public <E extends Throwable> ResultNoLogs<T> verify(Predicate<T> verification, Function<T, E> failureExceptionSupplier) {
			return verification.test(value)
				? this
				: ResultNoLogs.failure(failureExceptionSupplier.apply(value));
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

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
		public ResultNoLogs<T> filter(Predicate<T> filter) {
			return filter.test(value)
				? this
				: empty();
		}

		@Override
		public ResultNoLogs<String> forEachOrErrorMsg(Consumer<? super T> effect) {
			effect.accept(value);
			return empty();
		}

		@Override
		public ResultNoLogs<Throwable> forEachOrException(Consumer<? super T> effect) {
			effect.accept(value);
			return empty();
		}
	}
	public static class SuccessLogged<T> extends ResultNoLogs<T>{
		private final T value;
		private final LogEntry logEntry;

		private SuccessLogged(T value,LogEntry logEntry) {
			this.value = Objects.requireNonNull(value, "Success value is null, use an Empty value instead");
			this.logEntry = logEntry;
		}

		@Override
		public Optional<LogEntry> getSuccessLog() {
			return Optional.of(logEntry);
		}

		@Override
		public <U> ResultNoLogs<U> map(Function<T, U> mapper) {
			return success(mapper.apply(value),logEntry);
		}



		@Override
		public <U> ResultNoLogs<U> flatMap(Function<T, ResultNoLogs<U>> mapper) {
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
		public ResultNoLogs<T> mapError(Function<Throwable, ? extends Throwable> mapper) {
			return this;
		}

		@Override
		public String toString() {
			return "Success(" + value + ")";
		}

		@Override
		public <E extends Throwable> ResultNoLogs<T> verify(Predicate<T> verification, Function<T, E> failureExceptionSupplier) {
			return verification.test(value)
				? this
				: ResultNoLogs.failure(failureExceptionSupplier.apply(value));
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			SuccessLogged<?> success = (SuccessLogged<?>) o;

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
		public ResultNoLogs<T> filter(Predicate<T> filter) {
			return filter.test(value)
				? this
				: empty();
		}

		@Override
		public ResultNoLogs<String> forEachOrErrorMsg(Consumer<? super T> effect) {
			effect.accept(value);
			return empty();
		}

		@Override
		public ResultNoLogs<Throwable> forEachOrException(Consumer<? super T> effect) {
			effect.accept(value);
			return empty();
		}
	}

	public static class EmptyException extends RuntimeException{
		public EmptyException(){
			super("Can't get value from an Empty result!");
		}
	}

	public static class Empty<T> extends ResultNoLogs<T> {
		@Override
		public <U> ResultNoLogs<U> map(Function<T, U> mapper) {
			return new Empty<>();
		}

		@Override
		public <U> ResultNoLogs<U> flatMap(Function<T, ResultNoLogs<U>> mapper) {
			return new Empty<>();
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
			throw new EmptyException();
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
		public Optional<LogEntry> getSuccessLog() {
			return Optional.empty();
		}

		@Override
		public void ifFailure(Consumer<Throwable> e) {

		}

		@Override
		public ResultNoLogs<T> filter(Predicate<T> filter) {
			return this;
		}

		@Override
		public ResultNoLogs<T> mapError(Function<Throwable, ? extends Throwable> mapper) {
			return this;
		}

		@Override
		public <E extends Throwable> ResultNoLogs<T> verify(Predicate<T> verification, Function<T, E> failureExceptionSupplier) {
			return this;
		}

		@Override
		public ResultNoLogs<String> forEachOrErrorMsg(Consumer<? super T> effect) {
			return empty();
		}

		@Override
		public ResultNoLogs<Throwable> forEachOrException(Consumer<? super T> effect) {
			return empty();
		}
	}

	public static class FailureException extends RuntimeException{
		public FailureException(Throwable failureCause){
			super("Can't get value from a Failure Result",failureCause);
		}
	}

	public static class Failure<T> extends ResultNoLogs<T> {
		private final Throwable exception;

		public Failure(Throwable exception) {
			this.exception = Objects.requireNonNull(exception);
		}

		public Failure(String error) {
			this(new IllegalStateException(Objects.requireNonNull(error)));
		}


		@Override
		@SuppressWarnings("unchecked")
		public <U> ResultNoLogs<U> map(Function<T, U> mapper) {
			return (Failure<U>) this;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <U> ResultNoLogs<U> flatMap(Function<T, ResultNoLogs<U>> mapper) {
			return (Failure<U>) this;
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
			throw new FailureException( exception);
		}

		@Override
		public void ifEmpty(Runnable r) {

		}

		@Override
		public Optional<LogEntry> getSuccessLog() {
			return Optional.empty();
		}

		@Override
		public void ifFailure(Consumer<Throwable> e) {
			e.accept(exception);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

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
		public ResultNoLogs<T> filter(Predicate<T> filter) {
			return this;
		}

		@Override
		public ResultNoLogs<T> mapError(Function<Throwable, ? extends Throwable> mapper) {
			return ResultNoLogs.failure(mapper.apply(exception));
		}

		@Override
		public <E extends Throwable> ResultNoLogs<T> verify(Predicate<T> verification, Function<T, E> failureExceptionSupplier) {
			return this;
		}

		@Override
		public ResultNoLogs<String> forEachOrErrorMsg(Consumer<? super T> effect) {
			return ResultNoLogs.success(exception.getMessage());
		}

		@Override
		public ResultNoLogs<Throwable> forEachOrException(Consumer<? super T> effect) {
			return ResultNoLogs.success(exception);
		}
	}
}

