package com.persistentbit.core.experiments.easyscript;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 22/02/2017
 */
public class EEvalResult{

	private final EvalContext   context;
	private final Object value;

	public EEvalResult(EvalContext context, Object value) {
		this.context = context;
		this.value = value;
	}


	public EEvalResult withContext(EvalContext context) {
		return new EEvalResult(context, value);
	}

	public EvalContext getContext() {
		return context;
	}

	public Object getValue() { return value; }
}
