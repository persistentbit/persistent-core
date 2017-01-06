package com.persistentbit.core.result;

import com.persistentbit.core.logging.Log;
import com.persistentbit.core.logging.LogCleaner;
import com.persistentbit.core.logging.LogEntry;
import com.persistentbit.core.logging.LogEntryEmpty;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * A SUCCESS RESULT
 *
 * @param <T> Result Type
 */
public class Success<T> extends Result<T>{

    private final LogEntry log;
    private final T        value;

    private Success(T value, LogEntry log) {
        this.value = Objects.requireNonNull(value, "Success value is null, use an Empty value instead");
        this.log = Objects.requireNonNull(log, "Log must be non null");
    }



    Success(T value) {
        this(value, LogEntryEmpty.inst);
    }

    @Override
    public Result<T> mapLog(Function<LogEntry, LogEntry> mapper) {
        return new Success<>(value,mapper.apply(log));
    }

    @Override
    public Result<T> withLogs(Consumer<LogEntry> effect) {
        return Log.function().code(l -> {
            effect.accept(log);
            return Success.this;
        });

    }

    @Override
    public <U> Result<U> map(Function<T, U> mapper) {
        return new Success<>(mapper.apply(value),log);
    }
    @Override
    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        Result<U> res = mapper.apply(value);
        return res.mapLog(e -> log.append(e));
    }

    @Override
    public LogEntry getLog() {
        return log;
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

    @Override
    public Result<T> cleanLogsOnPresent() {
        return withLogs(l -> LogCleaner.clean(log));
    }
}