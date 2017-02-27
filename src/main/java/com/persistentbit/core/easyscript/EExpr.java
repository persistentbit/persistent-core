package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.ImmutableArray;
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
		Function<Group, T> group,
		Function<Lambda, T> lambda,
		Function<Const, T> constant,
		Function<Name, T> name,
		Function<Apply, T> apply,
		Function<Val, T> val,
		Function<ExprList, T> exprList,
		Function<Child, T> child,
		Function<Custom, T> custom
	);

	public static class Child extends EExpr{

		public final EExpr left;
		public final String childName;

		public Child(StrPos pos, EExpr left, String childName) {
			super(pos);
			this.left = left;
			this.childName = childName;
		}

		@Override
		public String toString() {
			return left + "." + childName;
		}

		@Override
		public <T> T match(Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList, Function<Child, T> child,Function<Custom, T> custom
		) {
			return child.apply(this);
		}
	}


	public static class ExprList extends EExpr{

		public final ImmutableArray<EExpr> expressions;

		public ExprList(StrPos pos, ImmutableArray<EExpr> expressions) {
			super(pos);
			this.expressions = expressions;
		}

		@Override
		public String toString() {
			return expressions.toString("; ");
		}

		@Override
		public <T> T match(Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList, Function<Child, T> child,Function<Custom, T> custom
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
		public <T> T match(Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList, Function<Child, T> child,Function<Custom, T> custom
		) {
			return group.apply(this);
		}
	}

	public static class Lambda extends EExpr{

		public final PList<String> paramNames;
		public final EExpr code;

		public Lambda(StrPos pos, PList<String> paramNames, EExpr code) {
			super(pos);
			this.paramNames = paramNames;
			this.code = code;
		}

		@Override
		public String toString() {
			return paramNames.toString("(", ", ", ")") + " -> { " + code + " }";
		}

		@Override
		public <T> T match(Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList, Function<Child, T> child,Function<Custom, T> custom
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
		public <T> T match(Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList, Function<Child, T> child,Function<Custom, T> custom
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
		public <T> T match(Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList, Function<Child, T> child,Function<Custom, T> custom
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
		public <T> T match(Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList, Function<Child, T> child,Function<Custom, T> custom
		) {
			return apply.apply(this);
		}
	}

	public static class Val extends EExpr{

		public enum Type{
			val, var, assign
		}

		public final String name;
		public final EExpr value;
		public final Type type;

		public Val(StrPos pos, String name, EExpr value, Type type) {
			super(pos);
			this.name = name;
			this.value = value;
			this.type = type;
		}

		public String toString() {
			switch(type) {
				case assign:
					return name + " = " + value;
				case val:
				case var:
				default:
					return type + " " + name + " = " + value;
			}

		}

		@Override
		public <T> T match(Function<Group, T> group, Function<Lambda, T> lambda,
						   Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply,
						   Function<Val, T> val, Function<ExprList, T> exprList, Function<Child, T> child,Function<Custom, T> custom
		) {
			return val.apply(this);
		}
	}
	public static class Custom extends EExpr{
		public final String name;
		public final PList<EExpr> arguments;

		public Custom(StrPos pos, String name, PList<EExpr> arguments) {
			super(pos);
			this.name = name;
			this.arguments = arguments;
		}

		@Override
		public String toString() {
			return "$" + name + arguments.toString("(",", ", ")");
		}

		@Override
		public <T> T match(Function<Group, T> group, Function<Lambda, T> lambda, Function<Const, T> constant, Function<Name, T> name, Function<Apply, T> apply, Function<Val, T> val, Function<ExprList, T> exprList, Function<Child, T> child,Function<Custom, T> custom) {
			return custom.apply(this);
		}
	}
}
