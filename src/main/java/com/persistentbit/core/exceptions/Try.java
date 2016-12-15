package com.persistentbit.core.exceptions;


import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * Try/Success/Failure Monad used to eliminating try/catch/throws .<br>
 * Based on Try in scala.<br>
 *
 * @see Success
 * @see Failure
 * @author Peter Muys
 * @since 12/12/2016
 */
public interface Try<R> extends Iterable<R> {

    /**
     * On Success, apply mapper.<br>
     * On Failure, return the original failure.<br>
     * @param mapper The mapper
     * @param <U> The new Try type
     * @return The mapped value.
     */
    <U> Try<U> flatMap(Function<R, Try<U>> mapper);

    /**
     * Apply the mapper and wrap in success or failure.<br>
     *
     * @param mapper The mapper
     * @param <U> The new Try type
     * @return The mapped value or the failure
     */
    <U> Try<U> map(Function<R, U> mapper);


    /**
     * Convert to Optional value on success and not null,
     * or an empty Optional.
     * @return The optional
     */
    Optional<R> getOpt();

    /**
     * Get the value on success or throw an exception
     * @param exceptionSupplier The supplier of an exception
     * @param <E> The type of exception
     * @return The value or nothing(exception)
     * @throws E Thrown on failure
     */
    default <E extends Throwable> R orElseThrow(Supplier<E> exceptionSupplier) throws E{
        if(isSuccess()){
            return getUnchecked();
        }
        throw exceptionSupplier.get();
    }

    /**
     * Get the success value or return the supplied value on failure
     * @param valueOnFailure The value to return on failure
     * @return The value or valueOnFailure
     */
    default R orElse(R valueOnFailure) {
        if(isSuccess()){
            return getUnchecked();
        }
        return valueOnFailure;
    }

    /**
     * Get the success value or ask the value on failure
     * @param supplierOnFailure The value supplier on failure
     * @return The resulting value
     */
    default R orElseGet(Supplier<R> supplierOnFailure){
        if(isSuccess()){
            return getUnchecked();
        }
        return supplierOnFailure.get();
    }

    /**
     * Filter the value.<br>
     *
     * @param filter The value filter
     * @return The success passing the filter  or a Failure
     */
    Try<R> filter(Predicate<R> filter);

    /**
     * Get the value or throw the Failure exception
     * @return The success value
     * @throws Throwable The Failure exception
     */
    R get() throws Throwable;

    /**
     * Get the value or throw an unchecked {@link TryFailureException} wrapping the Failure exception
     * @return
     */
    R getUnchecked();

    /**
     * Execute the code if this is a Success value
     * @param code The code to run
     */
    void ifSuccess(Consumer<R> code);

    /**
     * Execute the code if this is a Failure value
     * @param e The code to run
     */
    void ifFailure(Consumer<Throwable> e);

    boolean isSuccess();
    boolean isFailure();

    /**
     * Create a new Success result
     * @param value The success value
     * @param <R> The type of the Try
     * @return the Success value
     */
    static <R> Success<R> success(R value){
        return new Success<>(value);
    }

    /**
     * Create a Failure value
     * @param e  The Failure exception
     * @param <R> The Type of the Try
     * @return the Failure value
     */
    static <R> Failure<R> failure(Throwable e){
        return new Failure<>(e);
    }

    /**
     * Create a Failure value with a Runtime exception
     * @param message The Runtime exception value
     * @param <R> The Type of the Try
     * @return a new Failure
     */
    static <R> Failure<R> runtimeFailure(String message){
        return failure(new RuntimeException(message));
    }


    /**
     * Run the supplied code,
     * returning a {@link Failure} when there is an Exception or the return value as a {@link Success}
     *
     * @param code The code to execute
     * @param <R> The type of the return value
     * @return Success or Failure value
     */
    static <R> Try<R> run(TryCode<R> code){
        try{
            return new Success<>(code.run());
        }catch (Throwable e){
            return new Failure<>(e);
        }
    }

    /**
     * Run the supplied code,
     * returning a {@link Failure} when there is an Exception or the return value.
     * @param code The code to execute
     * @param <R> The type of the return value
     * @return Success or Failure value
     */
    static <R> Try<R> flatRun(TryCode<Try<R>> code){
        try{
            return code.run();
        }catch (Throwable e){
            return new Failure<>(e);
        }
    }

}
