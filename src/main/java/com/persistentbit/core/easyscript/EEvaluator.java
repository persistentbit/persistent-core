package com.persistentbit.core.easyscript;

import com.persistentbit.core.utils.StrPos;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/02/17
 */
public class EEvaluator{




	private EEvaluator() {}

	private static EEvaluator inst = new EEvaluator();

	public static EEvalResult eval(EvalContext context, EExpr expr) {
		return inst.evalExpr(context, expr);
	}

	private EEvalResult evalExpr(EvalContext context, EExpr expr) {
		return expr.match(
			e -> binOp(context, e),
			e -> group(context, e),
			e -> lambda(context, e),
			e -> constant(context, e),
			e -> name(context, e),
			e -> apply(context, e),
			e -> val(context, e),
			e -> exprList(context, e),
			e -> child(context, e)
		);
	}


	private EEvalResult binOp(EvalContext context, EExpr.BinOp e) {
		return evalExpr(context, e.left)
			.mapSuccess(left ->
				evalExpr(left.getContext(), e.right).mapSuccess(right ->
					executeFunction(right.getContext(),e.left.pos, left.getValue(), e.op, right.getValue())
				)
			);
	}

	private EEvalResult group(EvalContext context, EExpr.Group e) {
		return evalExpr(context.subContext(EvalContext.Type.block), e.expr);
	}

	private EEvalResult lambda(EvalContext context, EExpr.Lambda e) {
		return EEvalResult.success(context, new ERuntimeLambda(context.getLocalContext(),e.paramName,e.code));
	}

	private EEvalResult name(EvalContext context, EExpr.Name e) {
		Optional<Object> nameValue = context.getValue(e.name);
		if(nameValue.isPresent()) {
			return EEvalResult.success(context,nameValue.get());
		}
		return EEvalResult.failure(context,e.pos, "Undefined name:'" + e.name + "'");
	}

	private EEvalResult constant(EvalContext context, EExpr.Const e) {
		return EEvalResult.success(context, e.value);
	}

	private EEvalResult apply(EvalContext context, EExpr.Apply e) {
		return evalExpr(context,e.function).mapSuccess(rfun -> {
					return EEvalResult.todo(context);
				});
/*
			if(rfun instanceof ECallable){

			}
			if(rfun.getValue() instanceof ERuntimeLambda){
				ERuntimeLambda rtlambda = (ERuntimeLambda) rfun.getValue();
				if(e.parameters.isEmpty()){
					return EEvalResult.failure(context,e.pos,"Need at least 1 argument!");
				}
				EEvalResult result = rtlambda.apply(context,e.pos,e.parameters.head());
				if(result.isError()){
					return result;
				}
				if(e.parameters.size()>1){
					//Create and eval a new Apply...
					PList<EExpr> nextParamList = e.parameters.tail().plist();

					EExpr nextFunction = new EExpr.Const(e.pos,result.getValue());
					EExpr.Apply newApply = new EExpr.Apply(nextParamList.head().pos,nextFunction,nextParamList);
					return eval(result.getContext(),newApply);
				}
				return result;
			}
			return EEvalResult.todo(context);
		});*/
	}


	private EEvalResult val(EvalContext context, EExpr.Val e) {
		if(context.hasLocalValue(e.name)) {
			return EEvalResult.failure(context,e.pos,"val '" + e.name + "' is already defined!");
		}
		return evalExpr(context, e.value).mapSuccess(er -> er.withContext(context.withValue(e.name, er.getValue())));
	}

	private EEvalResult exprList(EvalContext context, EExpr.ExprList e) {
		return EEvalResult.todo(context);
	}


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
	}
	private EEvalResult child(EvalContext context, EExpr.Child e){
		EEvalResult parentResult = evalExpr(context,e.left);
		if(parentResult.isError()){
			return parentResult;
		}
		return ERuntimeChild.eval(parentResult.getValue(),e.childName,e.pos,parentResult.getContext());
	}
}
