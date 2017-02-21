package com.persistentbit.core.easyscript;

import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/02/17
 */
public class EvalException extends RuntimeException{

	public final StrPos pos;

	public EvalException(StrPos pos, String message) {
		super(pos + ": " + message);
		this.pos = pos;
	}
}
