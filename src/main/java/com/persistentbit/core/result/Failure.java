package com.persistentbit.core.result;

import com.persistentbit.core.logging.LogEntry;
import com.persistentbit.core.logging.LoggedException;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Failure<T> extends Result<T>{

    private final Throwable exception;
    private final LogEntry log;


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