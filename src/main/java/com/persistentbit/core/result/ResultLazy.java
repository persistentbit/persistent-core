package com.persistentbit.core.result;

import com.persistentbit.core.logging.LogEntry;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 4/01/2017
 */
public class ResultLazy<T> extends Result<T>{

	private Supplier<Result<T>> supplier;
	private Result<T>           value;

	private ResultLazy(Supplier<Result<T>> supplier, Result<T> value) {
		this.supplier = supplier;
	}

	private ResultLazy(Supplier<Result<T>> supplier) {
		this(supplier, null);
	}

	static public <T> ResultLazy<T> of(Supplier<Result<T>> supplier) {
		return new ResultLazy<>(supplier);
	}

	@Override
	public boolean isComplete() {
		return value != null && value.isComplete();
	}

	@Override
	public Result<T> completed() {
		return getValue().completed();
	}

	private synchronized Result<T> getValue() {
		if(value == null) {
			value = supplier.get();
		}
		return value;
	}

	@Override
	public LogEntry getLog() {
		return getValue().getLog();
	}

	@Override
	public <U> Result<U> map(Function<T, U> mapper) {
		return new ResultLazy<>(() -> getValue().map(mapper));
	}

	@Override
	public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
		return new ResultLazy<>(() -> getValue().flatMap(mapper));
	}

	@Override
	public Result<T> flatMapFailure(Function<? super Failure<T>, Result<T>> mapper) {
		return new ResultLazy<>(() -> getValue().flatMapFailure(mapper));
	}

	@Override
	public Result<T> flatMapEmpty(Function<? super Empty<T>, Result<T>> mapper) {
		return new ResultLazy<>(() -> getValue().flatMapEmpty(mapper));
	}

	@Override
	public Result<T> cleanLogsOnPresent() {
		return new ResultLazy<>(() -> getValue().cleanLogsOnPresent());
	}

	@Override
	public Optional<T> getOpt() {
		return getValue().getOpt();
	}

	@Override
	public boolean isEmpty() {
		return getValue().isEmpty();
	}

	@Override
	public Result<T> mapError(Function<Throwable, ? extends Throwable> mapper) {
		return new ResultLazy<>(() -> getValue().mapError(mapper));
	}

	@Override
	public <E extends Throwable> Result<T> verify(Predicate<T> verification, Function<T, E> failureExceptionSupplier) {
		return new ResultLazy<>(() -> getValue().verify(verification, failureExceptionSupplier));
	}

	@Override
	public Result<String> forEachOrErrorMsg(Consumer<? super T> effect) {
		return new ResultLazy<>(() -> getValue().forEachOrErrorMsg(effect));
	}

	@Override
	public Result<Throwable> forEachOrException(Consumer<? super T> effect) {
		return new ResultLazy<>(() -> getValue().forEachOrException(effect));
	}

	@Override
	public Result<T> mapLog(Function<LogEntry, LogEntry> mapper) {
		return new ResultLazy<>(() -> getValue().mapLog(mapper));
	}

	@Override
	public Result<T> withLogs(Consumer<LogEntry> effect) {
		return getValue().withLogs(effect);
	}

	@Override
	public T orElseThrow() {
		return getValue().orElseThrow();
	}

	@Override
	public Result<T> filter(Predicate<T> filter) {
		return new ResultLazy<>(() -> getValue().filter(filter));
	}

	@Override
	public void ifPresent(Consumer<T> effect) {
		getValue().ifPresent(effect);
	}

	@Override
	public void ifEmpty(Runnable r) {
		getValue().ifEmpty(r);
	}

	@Override
	public void ifFailure(Consumer<Throwable> e) {
		getValue().ifFailure(e);
	}

	@Override
	public String toString() {
		return "ResultLazy(" + getValue() + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Result == false) { return false; }
		Result other = (Result) obj;
		return completed().equals(other.completed());
	}

	@Override
	public int hashCode() {
		return completed().hashCode();
	}
}
