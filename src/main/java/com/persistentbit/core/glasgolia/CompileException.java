package com.persistentbit.core.glasgolia;

import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class CompileException extends RuntimeException{

	private StrPos pos;

	private static String msg(String message, StrPos pos) {
		return pos.toString() + ":" + message;
	}

	public CompileException(StrPos pos) {
		super(msg("Compile error", pos));
		this.pos = pos;
	}

	public CompileException(String message, StrPos pos) {
		super(msg(message, pos));
		this.pos = pos;
	}

	public CompileException(String message, Throwable cause, StrPos pos) {
		super(msg(message, pos), cause);
		this.pos = pos;
	}

	public CompileException(Throwable cause, StrPos pos) {
		super(cause);
		this.pos = pos;
	}

	public CompileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
							StrPos pos
	) {
		super(msg(message, pos), cause, enableSuppression, writableStackTrace);
		this.pos = pos;
	}

	public CompileException() {
		this(StrPos.inst);
	}

	public CompileException(String message) {
		this(message, StrPos.inst);
	}

	public CompileException(String message, Throwable cause) {
		this(message, cause, StrPos.inst);
	}

	public CompileException(Throwable cause) {
		this(cause, StrPos.inst);
	}

	public CompileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		this(message, cause, enableSuppression, writableStackTrace, StrPos.inst);
	}
}
