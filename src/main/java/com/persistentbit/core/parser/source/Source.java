package com.persistentbit.core.parser.source;

import com.persistentbit.core.parser.Parser;

import java.util.Objects;

/**
 * Represents the source and position in the source for a Parser.
 *
 * @author petermuys
 * @see Parser
 * @since 17/02/17
 */
public class Source{

	public static final char EOF = 0;

	private final ImmutableIterator<Character> iterator;
	private final Position position;


	private Source(ImmutableIterator<Character> iterator, Position position) {
		this.iterator = Objects.requireNonNull(iterator);
		this.position = Objects.requireNonNull(position);
	}

	public Source(String sourceName, ImmutableIterator<Character> iterator) {
		this(iterator, new Position(sourceName, 1, 1));
	}

	public static Source asSource(String name, String source) {
		return new Source(name, new StringImmutableIterator(source));
	}

	public Position getPosition() {
		return position;
	}

	public char current() {
		return iterator.orElse(EOF);
	}

	public Source next() {
		return new Source(iterator.next(), position.incForChar(current()));
	}

	@Override
	public String toString() {
		return "ParseSource[" + getPosition() + ", current=" + current() + "]";
	}
}
