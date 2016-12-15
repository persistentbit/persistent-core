package com.persistentbit.core.exceptions;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represent a Failure instance of a {@link Try}<br>
 * A Failure contains an Exception.<br>
 *
 * @author Peter Muys
 * @since 12/12/2016
 * @see Try
 * @see Success
 */
public class Failure<R> implements Try<R> {
    final private Throwable exception;


    public Failure(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "Failure(" + exception.getMessage() + ")";
    }


    @Override
    public <U> Try<U> flatMap(Function<R, Try<U>> mapper) {
        return (Try<U>)this;
    }

    @Override
    public <U> Try<U> map(Function<R, U> mapper) {
        return (Try<U>)this;
    }

    @Override
    public Optional<R> getOpt() {
        return Optional.empty();
    }

    @Override
    public R get() throws Throwable{
        throw exception;
    }

    @Override
    public R getUnchecked() {
        throw new TryFailureException("getUnchecked() on Failure", exception);
    }

    @Override
    public void ifSuccess(Consumer<R> code) {

    }

    @Override
    public Try<R> filter(Predicate<R> filter) {
        return this;
    }

    @Override
    public void ifFailure(Consumer<Throwable> e) {
        e.accept(exception);
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public Iterator<R> iterator() {
        return new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public R next() {
                throw new IllegalStateException("next() called on empty iterator for failure");
            }
        };
    }
}
