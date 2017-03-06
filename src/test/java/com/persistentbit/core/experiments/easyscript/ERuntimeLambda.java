package com.persistentbit.core.experiments.easyscript;

import com.persistentbit.core.collections.PList;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 22/02/2017
 */
public class ERuntimeLambda implements ECallable{

	public final EEvaluator evaluator;
	public final EvalContext localContext;
	public final PList<String> paramNames;
	public final EExpr code;

	public ERuntimeLambda(EEvaluator evaluator, EvalContext localContext, PList<String> paramNames, EExpr code) {
		this.evaluator = evaluator;
		this.localContext = localContext;
		this.paramNames = paramNames;
		this.code = code;
    }

    @Override
	public Object apply(Object... args) {
		EvalContext ctx = localContext;
		for(int t = 0; t < paramNames.size(); t++) {
			String name  = paramNames.get(t);
			Object value = args.length > t ? args[t] : null;
			ctx = ctx.withValue(name, value);
		}
		EEvalResult evalRes = evaluator.evalExpr(ctx, code);
		return evalRes.getValue();
	}


    @Override
    public String toString() {
		return "ERuntimeLambda[" + paramNames + "->" + code + "]";
	}
}
