package com.persistentbit.core.experiments.easyscript;

import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.ToDo;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/02/17
 */
public class EEvaluator{


	private EEvaluator() {

	}

	public static EEvaluator inst = new EEvaluator();

	public static <T> Result<T> eval(EvalContext context, EExpr expr) {
		return Result.function().code(l ->
			Result.success((T) inst.evalExpr(context, expr))
		);

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
		if(e.type == EExpr.Group.Type.block) {
			context = context.subContext(EvalContext.Type.block);
		}
		return evalExpr(context, e.expr);
	}

	private EEvalResult lambda(EvalContext context, EExpr.Lambda e) {
		return new EEvalResult(context, new ERuntimeLambda(this, context, e.paramNames, e.code));
	}

	private EEvalResult name(EvalContext context, EExpr.Name e) {
		if(context.hasValue(e.name)) {
			return new EEvalResult(context, context.getValue(e.name).orElse(null));
		}
		throw new EvalException(e.pos, "Undefined name:'" + e.name + "'");
	}

	private EEvalResult constant(EvalContext context, EExpr.Const e) {
		return new EEvalResult(context, e.value);
	}

	private EEvalResult apply(EvalContext context, EExpr.Apply e) {
		EEvalResult rfunRes = evalExpr(context, e.function);
		Object      rfun    = rfunRes.getValue();
		if(rfun instanceof ECallable) {
			EvalContext ctx      = rfunRes.getContext();
			ECallable   callable = (ECallable) rfun;
			Object[]    args     = new Object[e.parameters.size()];
			int         i        = 0;
			for(EExpr expr : e.parameters) {
				EEvalResult paramResult = evalExpr(ctx, expr);
				ctx = paramResult.getContext();
				args[i++] = paramResult.getValue();
			}
			return new EEvalResult(ctx, callable.apply(args));
		}
		if(rfun instanceof Class) {
			Class       cls  = (Class) rfun;
			EvalContext ctx  = rfunRes.getContext();
			Object[]    args = new Object[e.parameters.size()];
			int         i    = 0;
			for(EExpr expr : e.parameters) {
				EEvalResult paramResult = evalExpr(ctx, expr);

				ctx = paramResult.getContext();
				args[i++] = paramResult.getValue();
			}
			return new EEvalResult(ctx, EJavaObjectMethod.construct(e.pos, cls, args));
		}
		throw new ToDo("for " + rfun);

	}



	private EEvalResult val(EvalContext context, EExpr.Val e) {
		if(e.type != EExpr.Val.Type.assign && context.hasLocalValue(e.name)) {
			throw new EvalException(e.pos, "val '" + e.name + "' is already defined!");
		}
		EEvalResult er = evalExpr(context, e.value);
		return er.withContext(er.getContext().withValue(e.name, er.getValue()));
	}

	private EEvalResult exprList(EvalContext context, EExpr.ExprList e) {
		EEvalResult result = null;
		for(EExpr expr : e.expressions) {
			result = evalExpr(context, expr);
			context = result.getContext();
		}
		if(result == null) {
			return new EEvalResult(context, null);
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
		return ERuntimeChild.evalGetChild(parentResult.getValue(), e.childName, e.pos, parentResult.getContext());
	}


}
