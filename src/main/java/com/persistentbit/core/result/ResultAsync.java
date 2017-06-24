package com.persistentbit.core.result;

import com.persistentbit.core.logging.Log;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.tuples.Tuple2;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.*;

/**
 * A Result that get's it Result value from a {@link CompletableFuture}.
 * @see #completed()
 * @see #isComplete()
 * @author Peter Muys
 * @since 4/01/2017
 */
public class ResultAsync<T> extends Result<T> {
    private CompletableFuture<Result<T>> future;

    private ResultAsync(CompletableFuture<Result<T>> future){
        this.future = future;
    }

    static public <T> ResultAsync<T> of(CompletableFuture<Result<T>> future){
        return new ResultAsync<>(future);
    }

    static public <T> ResultAsync<T> of(Supplier<Result<T>> supplier){
        return of(CompletableFuture.supplyAsync(supplier));
    }

    static public <T> ResultAsync<T> of(Executor executor, Supplier<Result<T>> supplier) {
        return of(CompletableFuture.supplyAsync(supplier, executor));
    }

    @Override
    public LogEntry getLog() {
        return Log.function().code(l ->
            future.get().getLog()
        );
    }

    @Override
    public Optional<Throwable> getEmptyOrFailureException() {
        return Log.function().code(l -> future.get().getEmptyOrFailureException());
    }

    @Override
    public boolean isComplete() {
        if(future.isDone() == false){
            return false;
        }
        return Log.function().code(l -> future.get().isComplete());
    }

    /**
     * Execute the completable future to get the result value
     * @return The result value
     */
    @Override
    public Result<T> completed() {
        return Log.function().code(l -> future.get().completed());
    }

    @Override
    public <U> Result<U> map(Function<T, U> mapper) {
       return new ResultAsync<>(future.thenApplyAsync(r -> r.map(mapper)));
    }

    @Override
    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        return new ResultAsync<>(future.thenApplyAsync(r -> r.flatMap(mapper)));
    }

    @Override
    public Result<T> cleanLogsOnPresent() {
        return new ResultAsync<>(future.thenApplyAsync(r -> r.cleanLogsOnPresent()));
    }

    @Override
    public Optional<T> getOpt() {
        return Log.function().code(l -> future.get().getOpt());
    }

    @Override
    public boolean isEmpty() {
        return Log.function().code(l -> future.get().isEmpty());
    }

    @Override
    public Result<T> mapError(Function<Throwable, ? extends Throwable> mapper) {
        return new ResultAsync<>(future.thenApply(r -> r.mapError(mapper)));
    }

    @Override
    public <E extends Throwable> Result<T> verify(Predicate<T> verification, Function<T, E> failureExceptionSupplier) {
        return new ResultAsync<>(future.thenApply(r -> r.verify(verification,failureExceptionSupplier)));
    }

    @Override
    public Result<T> flatMapFailure(Function<? super Failure<T>, Result<T>> mapper
    ) {
        return new ResultAsync<>(future.thenApply(r -> r.flatMapFailure(mapper)));
    }

    @Override
    public Result<T> flatMapEmpty(Function<? super Empty<T>, Result<T>> mapper
    ) {
        return new ResultAsync<>(future.thenApply(r -> r.flatMapEmpty(mapper)));
    }

    @Override
    public Result<T> flatMapNoSuccess(BiFunction<Result<T>, Throwable, Result<T>> mapper) {
        return new ResultAsync<>(future.thenApply(r -> r.flatMapNoSuccess(mapper)));
    }

    @Override
    public Result<String> forEachOrErrorMsg(Consumer<? super T> effect) {
        return new ResultAsync<>(future.thenApply(r -> r.forEachOrErrorMsg(effect)));
    }

    @Override
    public Result<Throwable> forEachOrException(Consumer<? super T> effect) {
        return new ResultAsync<>(future.thenApply(r -> r.forEachOrException(effect)));
    }

    @Override
    public Result<T> mapLog(Function<LogEntry, LogEntry> mapper) {
        return new ResultAsync<>(future.thenApply(r -> r.mapLog(mapper)));
    }
    @Override
    public Result<T> doWithLogs(Consumer<LogEntry> effect) {
        return new ResultAsync<>(future.thenApply(r -> r.doWithLogs(effect)));
    }

    @Override
    public T orElseThrow() {
        return Log.function().code(l ->
                future.get().orElseThrow()
        );
    }

    @Override
    public Result<T> filter(Predicate<T> filter) {
        return new ResultAsync<>(future.thenApply(r -> r.filter(filter)));
    }


    @Override
    public Result<T> ifEmpty(Consumer<Empty<T>> effect) {
        return new ResultAsync<>(future.thenApply(r -> r.ifEmpty(effect)));
    }

    @Override
    public Result<T> ifFailure(Consumer<Failure<T>> effect) {
        return new ResultAsync<>(future.thenApply(r -> r.ifFailure(effect)));
    }

    @Override
    public Result<T> ifPresent(Consumer<Success<T>> effect) {
        return new ResultAsync<>(future.thenApply(r -> r.ifPresent(effect)));
    }

    @Override
    public String toString() {
        if(future.isDone() == false) {
            return "ResultAsync(<not done>)";
        }
        return Log.function().code(l-> "ResultAsync(" + future.get() + ")");
    }

    @Override
    public <U> Result<Tuple2<T, U>> combine(Result<U> otherResult) {
        if(otherResult instanceof ResultAsync){
            ResultAsync otherAsync = (ResultAsync) otherResult;

            return new ResultAsync<>(CompletableFuture.allOf(this.future,otherAsync.future)
                    .thenApply(completed -> super.combine(otherResult)));
        } else {
            return super.combine(otherResult);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Result == false) { return false; }
        Result other = (Result)obj;
        return completed().equals(other.completed());
    }

    @Override
    public int hashCode() {
        return completed().hashCode();
    }

    @Override
    public <U> U match(Function<Success<T>, U> onSuccess, Function<Empty<T>, U> onEmpty,
                       Function<Failure<T>, U> onFailure
    ) {
        return Log.function(this).code(l -> completed().match(onSuccess, onEmpty, onFailure));
    }
}
