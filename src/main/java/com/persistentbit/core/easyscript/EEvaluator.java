package com.persistentbit.core.easyscript;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/02/17
 */
public class EEvaluator{


	private EEvaluator() {

	}

	private static EEvaluator inst = new EEvaluator();

	public static EEvalResult eval(EvalContext context, EExpr expr) {
		try {
			return inst.evalExpr(context, expr);
		} catch(EvalException e) {
			return EEvalResult.failure(context, e);
		} catch(Exception e) {
			return EEvalResult.failure(context, expr.pos, "Evaluation failed");
		}
	}

	public EEvalResult evalExpr(EvalContext context, EExpr expr) {
		return expr.match(
			e -> group(context, e),
			e -> lambda(context, e),
			e -> constant(context, e),
			e -> name(context, e),
			e -> apply(context, e),
			e -> val(context, e),
			e -> exprList(context, e),
			e -> child(context, e),
			e -> ECustomExprEval.eval(this,context,e)
		);
	}



	private EEvalResult group(EvalContext context, EExpr.Group e) {
		return evalExpr(context.subContext(EvalContext.Type.block), e.expr);
	}

	private EEvalResult lambda(EvalContext context, EExpr.Lambda e) {
		return EEvalResult.success(context, new ERuntimeLambda(this, context.getLocalContext(), e.paramNames, e.code));
	}

	private EEvalResult name(EvalContext context, EExpr.Name e) {
		if(context.hasValue(e.name)) {
			return EEvalResult.success(context, context.getValue(e.name).orElse(null));
		}
		return EEvalResult.failure(context, e.pos, "Undefined name:'" + e.name + "'");
	}

	private EEvalResult constant(EvalContext context, EExpr.Const e) {
		return EEvalResult.success(context, e.value);
	}

	private EEvalResult apply(EvalContext context, EExpr.Apply e) {
		return evalExpr(context, e.function).mapSuccess(rfunRes -> {
			Object rfun = rfunRes.getValue();
			if(rfun instanceof ECallable) {
				EvalContext ctx      = rfunRes.getContext();
				ECallable   callable = (ECallable) rfun;
				Object[]    args     = new Object[e.parameters.size()];
				int         i        = 0;
				for(EExpr expr : e.parameters) {
					EEvalResult paramResult = evalExpr(ctx, expr);
					if(paramResult.isError()) {
						return paramResult;
					}
					ctx = paramResult.getContext();
					args[i++] = paramResult.getValue();
				}
				return EEvalResult.success(ctx, callable.apply(args));
			}
			if(rfun instanceof Class){
				Class cls = (Class)rfun;
				EvalContext ctx      = rfunRes.getContext();
				Object[]    args     = new Object[e.parameters.size()];
				int         i        = 0;
				for(EExpr expr : e.parameters) {
					EEvalResult paramResult = evalExpr(ctx, expr);
					if(paramResult.isError()) {
						return paramResult;
					}
					ctx = paramResult.getContext();
					args[i++] = paramResult.getValue();
				}
				return EEvalResult.success(ctx,EJavaObjectMethod.construct(e.pos,cls,args));
			}
			return EEvalResult.todo(context);
		});

	}



	private EEvalResult val(EvalContext context, EExpr.Val e) {
		if(e.type != EExpr.Val.Type.assign && context.hasLocalValue(e.name)) {
			return EEvalResult.failure(context, e.pos, "val '" + e.name + "' is already defined!");
		}
		return evalExpr(context, e.value)
			.mapSuccess(er -> er.withContext(er.getContext().withValue(e.name, er.getValue())));
	}

	private EEvalResult exprList(EvalContext context, EExpr.ExprList e) {
		EEvalResult result = null;
		for(EExpr expr : e.expressions) {
			result = evalExpr(context, expr);
			if(result.isError()) {
				return result;
			}
			context = result.getContext();
		}
		if(result == null) {
			return EEvalResult.success(context, null);
		}
		return result;
	}

	/*
		private EEvalResult executeFunction(EvalContext context, StrPos pos, Object obj, String name, Object arg) {
			if(obj == null) {
				return EEvalResult.failure(context,pos,"Can't execute function '" + name + "' on null object");
			}
			if(obj instanceof String) {
				return stringExecuteFunction(context,pos, (String) obj, name, arg);
			}
			else if(obj instanceof Integer) {
				return integerExecuteFunction(context,pos, (Integer) obj, name, arg);
			}
			return EEvalResult.failure(context,pos,"TODO for object:" + obj);
		}

		private EEvalResult stringExecuteFunction(EvalContext context, StrPos pos, String obj, String name, Object arg) {
			switch(name) {
				case "+":
					return EEvalResult.success(context, obj + arg);
				default:
					return EEvalResult.failure(context,pos, "Unknown function '" + name + "' for String");
			}
		}

		private EEvalResult integerExecuteFunction(EvalContext context,StrPos pos, Integer obj, String name, Object arg) {

			Integer rightNumber = arg instanceof Integer ? (Integer) arg : null;
			switch(name) {
				case "+":
					return EEvalResult.success(context, obj + rightNumber);
				case "-":
					return EEvalResult.success(context, obj - rightNumber);
				case "*":
					return EEvalResult.success(context, obj * rightNumber);
				case "/":
					return EEvalResult.success(context, obj / rightNumber);
				default:
					return EEvalResult.failure(context,pos,"Unknown function '" + name + "' for Integer");
			}
		}*/
	private EEvalResult child(EvalContext context, EExpr.Child e) {
		EEvalResult parentResult = evalExpr(context, e.left);
		if(parentResult.isError()) {
			return parentResult;
		}
		return ERuntimeChild.evalGetChild(parentResult.getValue(), e.childName, e.pos, parentResult.getContext());
	}


}
