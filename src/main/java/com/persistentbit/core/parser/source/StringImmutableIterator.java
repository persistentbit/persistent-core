package com.persistentbit.core.parser.source;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 19/02/17
 */
public class StringImmutableIterator implements ImmutableIterator<Character>{

	private final String value;
	private final int pos;

	private StringImmutableIterator(String value, int pos) {
		this.value = value;
		this.pos = pos;
	}

	public StringImmutableIterator(String value) {
		this(value, 0);
	}

	@Override
	public Character orElseThrow() {
		return value.charAt(pos);
	}

	@Override
	public Character orElse(Character elseValue) {
		if(pos < value.length()) {
			return value.charAt(pos);
		}
		return elseValue;
	}

	@Override
	public Optional<Character> getOpt() {
		if(pos < value.length()) {
			return Optional.ofNullable(value.charAt(pos));
		}
		return Optional.empty();
	}

	@Override
	public ImmutableIterator<Character> next() {
		return new StringImmutableIterator(value, pos + 1);
	}
}
