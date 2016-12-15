package com.persistentbit.core.exceptions;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A Try instance representing a success result<br>
 *
 * @author Peter Muys
 * @since 12/12/2016
 * @see Try
 * @see Failure
 */
public class Success<R> implements Try<R>{
    private final R result;

    public Success(R result) {
        this.result = result;
    }

    @Override
    public <U> Try<U> flatMap(Function<R, Try<U>> mapper) {
        try{
            return mapper.apply(result);
        }catch (Throwable e){
            return new Failure<>(e);
        }
    }

    @Override
    public <U> Try<U> map(Function<R, U> mapper) {
        try{
            return new Success<>(mapper.apply(result));
        }catch (Throwable e){
            return new Failure<>(e);
        }
    }



    @Override
    public void ifSuccess(Consumer<R> code) {
        code.accept(result);
    }

    @Override
    public void ifFailure(Consumer<Throwable> e) {
        return;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public String toString() {
        return "Success(" + result + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Success<?> success = (Success<?>) o;

        return result != null ? result.equals(success.result) : success.result == null;
    }

    @Override
    public int hashCode() {
        return result != null ? result.hashCode() : 0;
    }

    @Override
    public Optional<R> getOpt() {
        return Optional.ofNullable(result);
    }

    @Override
    public R getUnchecked() {
        return result;
    }

    @Override
    public R get() throws Throwable {
        return result;
    }

    @Override
    public Try<R> filter(Predicate<R> filter) {
        return Try.flatRun(()->
           filter.test(result) ? this : Try.failure(new TryFailureException("filtered Success"))
        );
    }

    @Override
    public Iterator<R> iterator() {
        return new Iterator<R>() {
            boolean first = true;
            @Override
            public boolean hasNext() {
                return first;
            }

            @Override
            public R next() {
                if(first == false){
                    throw new IllegalStateException("Only 1 item in Success");
                }
                first = false;
                return result;
            }
        };
    }
}
