package com.persistentbit.core.easyscript;

import com.persistentbit.core.utils.StrPos;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 22/02/2017
 */
public class ERuntimeLambda {
    public final EvalContext localContext;
    public final String paramName;
    public final EExpr code;

    public ERuntimeLambda(EvalContext localContext, String paramName, EExpr code) {
        this.localContext = localContext;
        this.paramName = paramName;
        this.code = code;
    }
    public EEvalResult apply(EvalContext context, StrPos pos,  EExpr argument){

        EEvalResult argRes = EEvaluator.eval(context,argument);
        if(argRes.isError()){
            return argRes;
        }
        EvalContext ctx = localContext.withParentContext(context).withValue(paramName,argRes.getValue());
        return EEvaluator.eval(ctx,code).withContext(ctx);
    }

    @Override
    public String toString() {
        return "RuntimeFunction[" + paramName + "->" + code+"]";
    }
}
