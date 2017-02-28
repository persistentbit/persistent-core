package com.persistentbit.core.easyscript;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 27/02/2017
 */
public class ECustomExprEval {

    public static EEvalResult eval(EEvaluator evaluator, EvalContext context, EExpr.Custom custom){
        switch (custom.name){
            case "if":
                return evalIf(evaluator,context,custom);
            case "ifElse":
                return evalIfElse(evaluator,context,custom);
			case "while":
				return evalWhile(evaluator, context, custom);
			case "import":
				return evalImport(evaluator, context, custom);
			case "source":
				return evalSource(evaluator, context, custom);
			case "classpath":
				return evalClasspath(evaluator, context, custom);
			default:
                throw new EvalException(custom.pos,"Unknown: " + custom.name);
        }
    }

    private static EEvalResult evalIf(EEvaluator evaluator,EvalContext context, EExpr.Custom custom) {
        EExpr condExpr = custom.arguments.get(0);
        EEvalResult condRes = evaluator.evalExpr(context, condExpr);
        if(condRes.isError()){
            return condRes;
        }
        if(condRes.getValue() instanceof Boolean == false){
            return EEvalResult.failure(context,condExpr.pos,"Expected a boolean expression for 'if' condition");
        }
        context = condRes.getContext();
        if(((Boolean)condRes.getValue())){
            return evaluator.evalExpr(context,custom.arguments.get(1));
        }
        return EEvalResult.success(context,null);

    }
    private static EEvalResult evalIfElse(EEvaluator evaluator,EvalContext context, EExpr.Custom custom) {
        EExpr condExpr = custom.arguments.get(0);
        EEvalResult condRes = evaluator.evalExpr(context, condExpr);
        if(condRes.isError()){
            return condRes;
        }
        if(condRes.getValue() instanceof Boolean == false){
            return EEvalResult.failure(context,condExpr.pos,"Expected a boolean expression for 'if' condition");
        }
        context = condRes.getContext();
        if(((Boolean)condRes.getValue())){
            return evaluator.evalExpr(context,custom.arguments.get(1));
        }
        return evaluator.evalExpr(context,custom.arguments.get(2));
    }

	private static EEvalResult evalWhile(EEvaluator evaluator, EvalContext context, EExpr.Custom custom) {
		EExpr  condExpr    = custom.arguments.get(0);
		EExpr  codeExpr    = custom.arguments.get(1);
		Object returnValue = null;
		while(true) {
			EEvalResult condRes = evaluator.evalExpr(context, condExpr);
			if(condRes.isError()) {
				return condRes;
			}
			if(condRes.getValue() instanceof Boolean == false) {
				return EEvalResult.failure(context, condExpr.pos, "Expected a boolean as while condition");
			}
			Boolean condValue = (Boolean) (condRes.getValue());
			if(condValue == false) {
				return EEvalResult.success(context, returnValue);
			}
			context = condRes.getContext();
			EEvalResult codeRes = evaluator.evalExpr(context, codeExpr);
			if(codeRes.isError()) {
				return codeRes;
			}
			context = codeRes.getContext();
			returnValue = codeRes.getValue();
		}

	}

	private static EEvalResult evalImport(EEvaluator evaluator, EvalContext context, EExpr.Custom custom) {
		String importName = evaluator.evalExpr(context, custom.arguments.get(0)).getValue().toString();
		context = context.addImport(importName);
		return EEvalResult.success(context, importName);
	}

	private static EEvalResult evalSource(EEvaluator evaluator, EvalContext context, EExpr.Custom custom) {
		throw new RuntimeException("ECustomExprEval.evalWhile TODO: Not yet implemented");
	}

	private static EEvalResult evalClasspath(EEvaluator evaluator, EvalContext context, EExpr.Custom custom) {
		throw new RuntimeException("ECustomExprEval.evalWhile TODO: Not yet implemented");
	}

}
