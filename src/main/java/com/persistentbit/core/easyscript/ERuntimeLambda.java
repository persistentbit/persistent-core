package com.persistentbit.core.easyscript;

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
	public Object apply(Object... args) {
		if(args.length == 0) {
			throw new EvalException(code.pos, "Can't call a lambda without arguments");
		}
		EvalContext ctx        = localContext.withValue(paramName, args[0]);
		Object      thisResult = EEvaluator.eval(ctx, code).getValue();
		if(args.length == 1) {
			return thisResult;
		}
		if(thisResult instanceof ECallable == false) {
			throw new EvalException(code.pos, "Expected a function as a result of applying " + args[0]);
		}
		ECallable callable = (ECallable) thisResult;
		Object[]  newArgs  = new Object[args.length - 1];
		System.arraycopy(args, 1, newArgs, 0, args.length - 1);
		thisResult = callable.apply(newArgs);
		return thisResult;
	}


    @Override
    public String toString() {
		return "ERuntimeLambda[" + paramName + "->" + code + "]";
	}
}
