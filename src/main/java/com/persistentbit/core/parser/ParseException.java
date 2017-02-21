package com.persistentbit.core.parser;

import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 19/02/17
 */
public class ParseException extends RuntimeException{

	private final StrPos pos;

	public ParseException(String message, StrPos pos) {
		super(pos + ": " + message);
		this.pos = pos;
	}

	public ParseException(String message, Throwable cause, StrPos pos) {
		super(message, cause);
		this.pos = pos;
	}

	public ParseException withCause(ParseException cause) {
		return new ParseException(this.getMessage(), cause, pos);
	}
}
