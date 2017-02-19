package com.persistentbit.core.parser;

import com.persistentbit.core.parser.source.Position;

/**
 * TODOC
 *
 * @author petermuys
 * @since 19/02/17
 */
public class ParseException extends RuntimeException{

	private final Position pos;

	public ParseException(String message, Position pos) {
		super(pos + ": " + message);
		this.pos = pos;
	}

	public ParseException(String message, Throwable cause, Position pos) {
		super(message, cause);
		this.pos = pos;
	}

	public ParseException withCause(ParseException cause) {
		return new ParseException(this.getMessage(), cause, pos);
	}
}
