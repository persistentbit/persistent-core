package com.persistentbit.core.utils;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Either 1 of 2 possible values.<br>
 * The two values are named left and right.<br>
 * The convention is to use the left value when there is an error
 * and the right value when everything is alRIGHT.
 * @author petermuys
 * @since 27/12/16
 */
public abstract class Either<T, U> implements Serializable{

	/**
	 * Map the value of right
	 * @param f mapper function
	 * @param <B> The new Right value type
	 * @return The new Either
	 */
	public abstract <B> Either<T, B> map(Function<U, B> f);

	/**
	 * Flatmap the right value
	 * @param f the mapper
	 * @param <B> the new type for the right value
	 * @return The new Either
	 */
	public abstract <B> Either<T, B> flatMap(Function<U, Either<T, B>> f);

	public abstract U orElse(U elseValue);

	public abstract U orElseGet(Supplier<? extends U> supplier);

	public abstract <X extends Throwable> U orElseThrow(Supplier<? extends X> exceptionSupplier) throws X;

	public abstract Optional<T> leftOpt();

	public abstract Optional<U> rightOpt();

	private static class Left<T, U> extends Either<T, U>{

		private final T value;

		public Left(T value) {
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public <B> Either<T, B> map(Function<U, B> f) {
			return new Left<>(value);
		}

		@Override
		public <B> Either<T, B> flatMap(Function<U, Either<T, B>> f) {
			return new Left<>(value);
		}




		@Override
		public Optional<T> leftOpt() {
			return Optional.ofNullable(value);
		}

		@Override
		public Optional<U> rightOpt() {
			return Optional.empty();
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			Left<?, ?> left = (Left<?, ?>) o;

			return Objects.equals(value, left.value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public String toString() {
			return "Left(" + value + ")";
		}

		@Override
		public U orElse(U elseValue) {
			return elseValue;
		}

		@Override
		public U orElseGet(Supplier<? extends U> supplier) {
			return supplier.get();
		}

		@Override
		public <X extends Throwable> U orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
			throw exceptionSupplier.get();
		}
	}

	private static class Right<T, U> extends Either<T, U>{

		private final U value;

		public Right(U value) {
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public <B> Either<T, B> map(Function<U, B> f) {
			return new Right<>(f.apply(value));
		}

		@Override
		public <B> Either<T, B> flatMap(Function<U, Either<T, B>> f) {
			return f.apply(value);
		}

		@Override
		public U orElse(U elseValue) {
			return value;
		}

		@Override
		public U orElseGet(Supplier<? extends U> supplier) {
			return value;
		}

		@Override
		public <X extends Throwable> U orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
			return value;
		}

		@Override
		public Optional<T> leftOpt() {
			return Optional.empty();
		}

		@Override
		public Optional<U> rightOpt() {
			return Optional.ofNullable(value);
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			Right<?, ?> right = (Right<?, ?>) o;

			return Objects.equals(value, right.value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public String toString() {
			return "Right(" + value + ")";
		}
	}

	public static <T, U> Either<T, U> left(T left) {
		return new Left<>(left);
	}

	public static <T, U> Either<T, U> right(U right) {
		return new Right<>(right);
	}
}
