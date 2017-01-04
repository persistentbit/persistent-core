package com.persistentbit.core.result;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.logging.LogEntry;
import com.persistentbit.core.logging.LogEntryEmpty;
import com.persistentbit.core.logging.LoggedValue;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.tuples.Tuple3;
import com.persistentbit.core.tuples.Tuple4;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
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
public abstract class Result<T> implements Iterable<T>, Serializable, LoggedValue<Result<T>> {


    /**
     * Map the Success value or return a Failure or a Empty
     *
     * @param mapper the value mapper
     * @param <U>    The resulting value type
     * @return a new Result
     */
    public abstract <U> Result<U> map(Function<T, U> mapper);

    /**
     * Flatmap the Success value or return a Failure or a Empty
     *
     * @param mapper the value mapper
     * @param <U>    The resulting value type
     * @return a new Result
     */
    public abstract <U> Result<U> flatMap(Function<T, Result<U>> mapper);

    /**
     * Convert this result to an optional
     *
     * @return Some value for success or an empty optional
     */
    public abstract Optional<T> getOpt();


    public boolean isComplete(){
        return true;
    }
    public Result<T> completed(){
        return this;
    }


    public Result<T> ifPresent(Consumer<T> effect) {
        getOpt().ifPresent(effect);
        return this;
    }

    static public final Success<Nothing> NothingSuccess = new Success<>(Nothing.inst);



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


    public <U> Result<Tuple2<T, U>> combine(Result<U> otherResult) {
        Result<Tuple2<T, U>> result = flatMap(thisValue ->{
                return otherResult.flatMap(otherValue -> {
                    return Result.success(Tuple2.of(thisValue, otherValue));
                });
        });
        return result;
    }
    public <B,C> Result<Tuple3<T,B,C>> combine(Result<B> rb, Result<C> rc){
        return combine(rb).combine(rc)
                .map(t -> Tuple3.of(t._1._1,t._1._2,t._2));
    }

    public <B,C,D> Result<Tuple4<T,B,C,D>> combine(Result<B> rb, Result<C> rc, Result<D> rd){
        return combine(rb,rc).combine(rd).map(t -> Tuple4.of(t._1._1,t._1._2,t._1._3,t._2));
    }


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
     * @return a Success
     */
    public static <U> Success<U> success(U value) {
        return new Success<>(value);
    }


    /**
     * Create an Empty result
     *
     * @param <U> The normal value type
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


    /**
     * Create a Success or Empty result
     *
     * @param value The Nullable value
     * @param <U>   The type of the value
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
     * @return a Failure result
     */
    public static <U> Failure<U> failure(Throwable exception) {
        return new Failure<>(exception, LogEntryEmpty.inst);
    }

    public static <U> ResultAsync<U> async(Supplier<Result<U>> code) {
        return ResultAsync.of(code);
    }

    public static <U> ResultLazy<U> lazy(Supplier<Result<U>> lazy){
        return ResultLazy.of(lazy);
    }


    /**
     * Create a new Function, returning a Result over the return type of the supplied function.<br>
     *
     * @param f   The function to convert
     * @param <T> The function argument type
     * @param <R> The Result value type
     * @return A new function returning a Result
     */
    public static <T, R> Function<T, Result<R>> toResult(Function<T, R> f) {
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

    public static <T, U, R> Function<T, Function<U, Result<R>>> higherToResult(Function<T, Function<U, R>> f) {
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


    /**
     * Exception thrown when we try to get a value from an {@link Empty}
     */
    public static class EmptyException extends RuntimeException {

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
    public static class FailureException extends RuntimeException {

        public FailureException(Throwable failureCause) {
            super("Can't get value from a Failure Result", failureCause);
        }
    }


}
