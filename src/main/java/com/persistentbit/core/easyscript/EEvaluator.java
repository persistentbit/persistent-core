package com.persistentbit.core.easyscript;

import com.persistentbit.core.result.Result;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/02/17
 */
public class EEvaluator{


	public static class EvalResult{

		public final EvalContext context;
		public final Object value;

		public EvalResult(EvalContext context, Object value) {
			this.context = context;
			this.value = value;
		}

		public EvalResult withContext(EvalContext context) {
			return new EvalResult(context, value);
		}

		public EvalResult withValue(Object value) {
			return new EvalResult(context, value);
		}
	}

	private EEvaluator() {}

	private static EEvaluator inst = new EEvaluator();

	public static Result<EvalResult> eval(EvalContext context, EExpr expr) {
		return inst.evalExpr(context, expr);
	}

	private Result<EvalResult> evalExpr(EvalContext context, EExpr expr) {
		return expr.match(
			e -> binOp(context, e),
			e -> group(context, e),
			e -> lambda(context, e),
			e -> constant(context, e),
			e -> name(context, e),
			e -> apply(context, e),
			e -> val(context, e),
			e -> exprList(context, e)
		);
	}

	private Result<EvalResult> binOp(EvalContext context, EExpr.BinOp e) {
		return evalExpr(context, e.left)
			.flatMap(left ->
				evalExpr(left.context, e.right).flatMap(right ->
					executeFunction(right.context, left.value, e.op, right.value)
				)
			);
	}

	private Result<EvalResult> group(EvalContext context, EExpr.Group e) {
		return evalExpr(context.subContext(EvalContext.Type.block), e.expr);
	}

	private Result<EvalResult> lambda(EvalContext context, EExpr.Lambda e) {
		return Result.success(new EvalResult(context, e));
	}

	private Result<EvalResult> name(EvalContext context, EExpr.Name e) {
		Optional<Object> nameValue = context.getValue(e.name);
		if(nameValue.isPresent()) {
			return Result.success(new EvalResult(context, nameValue.get()));
		}
		return Result.failure(new EvalException(e.pos, "Undefined name:'" + e.name + "'"));
	}

	private Result<EvalResult> constant(EvalContext context, EExpr.Const e) {
		return Result.success(new EvalResult(context, e.value));
	}

	private Result<EvalResult> apply(EvalContext context, EExpr.Apply e) {
		return Result.TODO();
		/*return evalExpr(context,e.function)
			.flatMap(funRes -> {
				EvalContext nc = funRes.context;
				Result<EvalResult> result = Result.success(funRes);
				for(EExpr arg : e.parameters){
					result = evalExpr(nc,arg)
						.flatMap(argRes-> evalExpr(argRes.context,))

				}
			});*/
	}


	private Result<EvalResult> val(EvalContext context, EExpr.Val e) {
		if(context.hasLocalValue(e.name)) {
			return Result.failure("val '" + e.name + "' is already defined!");
		}
		return evalExpr(context, e.value)
			.map(er -> er.withContext(context.withValue(e.name, er.value)));
	}

	private Result<EvalResult> exprList(EvalContext context, EExpr.ExprList e) {
		return Result.TODO();
	}


	private Result<EvalResult> executeFunction(EvalContext context, Object obj, String name, Object arg) {
		if(obj == null) {
			return Result.failure("Can't execute function '" + name + "' on null object");
		}
		if(obj instanceof String) {
			return stringExecuteFunction(context, (String) obj, name, arg);
		}
		else if(obj instanceof Integer) {
			return integerExecuteFunction(context, (Integer) obj, name, arg);
		}
		return Result.failure("TODO for object:" + obj);
	}

	private Result<EvalResult> stringExecuteFunction(EvalContext context, String obj, String name, Object arg) {
		switch(name) {
			case "+":
				return Result.success(new EvalResult(context, obj + arg));
			default:
				return Result.failure("Unknown function '" + name + "' for String");
		}
	}

	private Result<EvalResult> integerExecuteFunction(EvalContext context, Integer obj, String name, Object arg) {

		Integer rightNumber = arg instanceof Integer ? (Integer) arg : null;
		switch(name) {
			case "+":
				return Result.success(new EvalResult(context, obj + rightNumber));
			case "-":
				return Result.success(new EvalResult(context, obj - rightNumber));
			case "*":
				return Result.success(new EvalResult(context, obj * rightNumber));
			case "/":
				return Result.success(new EvalResult(context, obj / rightNumber));
			default:
				return Result.failure("Unknown function '" + name + "' for Integer");
		}
	}

}
