package com.persistentbit.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 27/12/2016
 */
public abstract class Result<T> implements Iterable<T>,Serializable {


    public abstract <U> Result<U> map(Function<T,U> mapper);
    public abstract <U> Result<U> flatMap(Function<T,Result<U>> mapper);
    public abstract Optional<T>  getOpt();


    public T orElse(T value){
        return getOpt().orElse(value);
    }

    public <E extends Throwable> T orElseThrow(Supplier<? extends E> exceptionSupplier) throws E{
        return getOpt().orElseThrow(exceptionSupplier);
    }

    @Override
    public Iterator<T> iterator() {
        return getOpt().map(v -> Collections.singletonList(v).iterator()).orElseGet(Collections::emptyIterator);
    }

    public abstract T orElseThrow();

    public abstract void ifEmpty(Runnable r);
    public abstract void ifPresent(Consumer<T> f);
    public abstract void ifFailure(Consumer<RuntimeException> e);


    public static <U> Success<U> success(U value){
        return new Success<>(value);
    }
    public static <U> Empty<U> empty() { return new Empty<U>(); }
    public static <U> Result<U> result(U value) {
        return value == null ? empty() : success(value);
    }

    public static <U> Failure<U> failure(String error){
        return new Failure<>(error);
    }


    static <T,R> Function<T,Result<R>> toResult(Function<T,R> f){
        return x -> {
            try{
                return result(f.apply(x));
            }catch (Exception e){
                return failure(e.getMessage());
            }
        };

    }

    public static class Success<T> extends Result<T>{
        private final T value;

        private Success(T value) {
            this.value = Objects.requireNonNull(value,"Success value is null, use an Empty value instead");
        }

        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            return success(mapper.apply(value));
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            throw new RuntimeException("Success.flatMap TODO: Not yet implemented");
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
        public String toString() {
            return "Success(" + value + ")";
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
        public void ifPresent(Consumer<T> f) {
            f.accept(value);
        }

        @Override
        public void ifFailure(Consumer<RuntimeException> e) {

        }
    }

    public static class Empty<T> extends Result<T>{
        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            return new Empty<>();
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            return new Empty<>();
        }

        @Override
        public Optional<T> getOpt() {
            return Optional.empty();
        }

        @Override
        public T orElseThrow() {
            throw new IllegalStateException("Can't get value from an Empty!");
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
        public void ifPresent(Consumer<T> f) {

        }

        @Override
        public void ifFailure(Consumer<RuntimeException> e) {

        }
    }

    public static class Failure<T> extends Result<T>{
        private final RuntimeException exception;

        public Failure(String error) {
            this(new IllegalStateException(Objects.requireNonNull(error)));
        }

        public Failure(RuntimeException exception){
            this.exception = Objects.requireNonNull(exception);
        }

        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            return (Failure<U>)this;
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            return (Failure<U>)this;
        }

        @Override
        public Optional<T> getOpt() {
            return Optional.empty();
        }



        public T orElseThrow() {
            throw exception;
        }

        @Override
        public void ifEmpty(Runnable r) {

        }

        @Override
        public void ifPresent(Consumer<T> f) {

        }

        @Override
        public void ifFailure(Consumer<RuntimeException> e) {
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
    }
}
