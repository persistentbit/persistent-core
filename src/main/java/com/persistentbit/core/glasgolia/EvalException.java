package com.persistentbit.core.glasgolia;

import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class EvalException extends RuntimeException{

	private StrPos pos;

	private static String msg(String message, StrPos pos) {
		return pos.toString() + ":" + message;
	}

	public EvalException(StrPos pos) {
		super(msg("Evaluation error", pos));
		this.pos = pos;
	}

	public EvalException(String message, StrPos pos) {
		super(msg(message, pos));
		this.pos = pos;
	}

	public EvalException(String message, Throwable cause, StrPos pos) {
		super(msg(message, pos), cause);
		this.pos = pos;
	}

	public EvalException(Throwable cause, StrPos pos) {
		super(cause);
		this.pos = pos;
	}

	public EvalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
						 StrPos pos
	) {
		super(msg(message, pos), cause, enableSuppression, writableStackTrace);
		this.pos = pos;
	}

	public EvalException() {
		this(StrPos.inst);
	}

	public EvalException(String message) {
		this(message, StrPos.inst);
	}

	public EvalException(String message, Throwable cause) {
		this(message, cause, StrPos.inst);
	}

	public EvalException(Throwable cause) {
		this(cause, StrPos.inst);
	}

	public EvalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		this(message, cause, enableSuppression, writableStackTrace, StrPos.inst);
	}
}
