package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.StringUtils;

import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/02/2017
 */
public abstract class EExpr {

	public final StrPos pos;

	public EExpr(StrPos pos) {
		this.pos = pos;
	}

	public abstract <T> T match(
		Function<BinOp, T> binOp,
		Function<Group, T> group,
		Function<Lambda, T> lambda,
		Function<Const, T> constant,
		Function<Name, T> name,
		Function<Apply, T> apply,
		Function<Val, T> val,
		Function<ExprList, T> exprList
	);

	public static class BinOp extends EExpr{

		public final EExpr left;
		public final String op;
		public final EExpr right;

		public BinOp(StrPos pos, EExpr left, String op, EExpr right) {
			super(pos);
			this.left = left;
			this.op = op;
			this.right = right;
		}

		@Override
		public String toString() {
			return left + " " + op + " " + right;
		}


		@Override
		public <T> T match(Function<BinOp, T> binOp, Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList
		) {
			return binOp.apply(this);
		}
	}

	public static class ExprList extends EExpr{

		public final PList<EExpr> list;

		public ExprList(StrPos pos, PList<EExpr> list) {
			super(pos);
			this.list = list;
		}

		@Override
		public String toString() {
			return list.toString("[", ", ", "]");
		}

		@Override
		public <T> T match(Function<BinOp, T> binOp, Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList
		) {
			return exprList.apply(this);
		}
	}

	public static class Group extends EExpr{

		public enum Type{group, block}

		public final EExpr expr;
		public final Type type;

		public Group(StrPos pos, EExpr expr, Type type) {
			super(pos);
			this.expr = expr;
			this.type = type;
		}

		@Override
		public String toString() {
			return type == Type.group
				? "(" + expr + ")"
				: "{" + expr + "}";
		}

		@Override
		public <T> T match(Function<BinOp, T> binOp, Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList
		) {
			return group.apply(this);
		}
	}

	public static class Lambda extends EExpr{

		public final String paramName;
		public final EExpr code;

		public Lambda(StrPos pos, String paramName, EExpr code) {
			super(pos);
			this.paramName = paramName;
			this.code = code;
		}

		@Override
		public String toString() {
			return paramName + " -> { " + code + " }";
		}

		@Override
		public <T> T match(Function<BinOp, T> binOp, Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList
		) {
			return lambda.apply(this);
		}
	}

	public static class Const extends EExpr{

		public final Object value;

		public Const(StrPos pos, Object value) {
			super(pos);
			this.value = value;
		}

		@Override
		public String toString() {
			if(value instanceof String) {
				return "\"" + StringUtils.escapeToJavaString(value.toString()) + "\"";
			}
			else {
				return "" + value;
			}
		}

		@Override
		public <T> T match(Function<BinOp, T> binOp, Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList
		) {
			return constant.apply(this);
		}
	}

	public static class Name extends EExpr{

		public final String name;

		public Name(StrPos pos, String name) {
			super(pos);
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public <T> T match(Function<BinOp, T> binOp, Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList
		) {
			return name.apply(this);
		}
	}

	public static class Apply extends EExpr{

		public final EExpr function;
		public final PList<EExpr> parameters;

		public Apply(StrPos pos, EExpr function,
					 PList<EExpr> parameters
		) {
			super(pos);
			this.function = function;
			this.parameters = parameters;
		}

		@Override
		public String toString() {
			return function + parameters.toString("(", ", ", ")");
		}

		@Override
		public <T> T match(Function<BinOp, T> binOp, Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList
		) {
			return apply.apply(this);
		}
	}

	public static class Val extends EExpr{

		public final String name;
		public final EExpr value;

		public Val(StrPos pos, String name, EExpr value) {
			super(pos);
			this.name = name;
			this.value = value;
		}

		public String toString() {
			return "val " + name + " = " + value;
		}

		@Override
		public <T> T match(Function<BinOp, T> binOp, Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList
		) {
			return val.apply(this);
		}
	}

}
