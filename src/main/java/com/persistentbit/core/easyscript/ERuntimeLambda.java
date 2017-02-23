package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.StrPos;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 22/02/2017
 */
public class ERuntimeLambda implements ECallable{
    public final EvalContext localContext;
    public final String paramName;
    public final EExpr code;

    public ERuntimeLambda(EvalContext localContext, String paramName, EExpr code) {
        this.localContext = localContext;
        this.paramName = paramName;
        this.code = code;
    }

    @Override
    public EEvalResult apply(EvalContext context, StrPos pos, PList<Object> args) {
        EvalContext ctx = localContext.withParentContext(context);
        return EEvaluator.eval(ctx,code).withContext(ctx);
    }

    @Override
    public String toString() {
        return "RuntimeFunction[" + paramName + "->" + code+"]";
    }
}
