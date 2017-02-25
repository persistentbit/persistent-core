package com.persistentbit.core.parser;

import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 24/02/17
 */
public class ParseExceptionEOF extends ParseException{

	public ParseExceptionEOF(String message, StrPos pos) {
		super(message, pos);
	}

	public ParseExceptionEOF(String message, Throwable cause, StrPos pos) {
		super(message, cause, pos);
	}
}
