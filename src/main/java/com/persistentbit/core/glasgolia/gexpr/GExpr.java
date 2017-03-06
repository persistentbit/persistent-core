package com.persistentbit.core.glasgolia.gexpr;

import com.persistentbit.core.collections.ImmutableArray;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.glasgolia.ETypeSig;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UString;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/03/17
 */
public abstract class GExpr{

	private GExpr() {}

	public abstract ETypeSig getType();

	public abstract StrPos getPos();


	static public class TypedName{

		public StrPos pos;
		public String name;
		public ETypeSig type;

		public TypedName(StrPos pos, String name, ETypeSig type) {
			this.pos = pos;
			this.name = name;
			this.type = type;
		}

		@Override
		public String toString() {
			return name + ":" + type;
		}
	}

	public abstract <T> T match(
		Function<Child, T> matchChild,
		Function<ExprList, T> matchExprList,
		Function<Group, T> matchGroup,
		Function<Const, T> matchConst,
		Function<Name, T> matchName,
		Function<Apply, T> matchApply,
		Function<ValVar, T> matchValVar,
		Function<BinOp, T> matchBinOp,
		Function<Cast, T> matchCast,
		Function<Custom, T> matchCustom,
		Function<Lambda, T> matchLambda
	);

	public static class Child extends GExpr{

		public final GExpr left;
		public final TypedName childName;

		public Child(StrPos pos, GExpr left, TypedName childName) {
			this.left = left;
			this.childName = childName;
		}

		@Override
		public ETypeSig getType() {
			return ETypeSig.any;
		}

		@Override
		public StrPos getPos() {
			return left.getPos();
		}

		@Override
		public String toString() {
			return left + "." + childName;
		}


		@Override
		public <T> T match(Function<Child, T> matchChild, Function<ExprList, T> matchExprList,
						   Function<Group, T> matchGroup,
						   Function<Const, T> matchConst, Function<Name, T> matchName,
						   Function<Apply, T> matchApply, Function<ValVar, T> matchValVar,
						   Function<BinOp, T> matchBinOp, Function<Cast, T> matchCast,
						   Function<Custom, T> matchCustom, Function<Lambda, T> matchLambda
		) {
			return matchChild.apply(this);
		}
	}


	public static class ExprList extends GExpr{

		private final StrPos pos;
		public final ImmutableArray<GExpr> expressions;

		public ExprList(StrPos pos, ImmutableArray<GExpr> expressions) {
			this.pos = pos;
			this.expressions = expressions;
		}


		@Override
		public String toString() {
			return expressions.match(
				() -> "()",
				single -> single.toString(),
				list -> list.toString("; ")
			);
		}

		@Override
		public ETypeSig getType() {
			return expressions.lastOpt().map(e -> e.getType()).orElse(ETypeSig.any);
		}

		@Override
		public StrPos getPos() {
			return pos;
		}

		@Override
		public <T> T match(Function<Child, T> matchChild, Function<ExprList, T> matchExprList,
						   Function<Group, T> matchGroup,
						   Function<Const, T> matchConst, Function<Name, T> matchName,
						   Function<Apply, T> matchApply, Function<ValVar, T> matchValVar,
						   Function<BinOp, T> matchBinOp, Function<Cast, T> matchCast,
						   Function<Custom, T> matchCustom, Function<Lambda, T> matchLambda
		) {
			return matchExprList.apply(this);
		}
	}

	public static class Group extends GExpr{

		public enum GroupType{group, block}

		public final GExpr expr;
		public final GroupType groupType;

		public Group(GExpr expr, GroupType groupType) {
			this.expr = expr;
			this.groupType = groupType;
		}

		@Override
		public String toString() {
			return groupType == GroupType.group
				? "(" + expr + ")"
				: "{" + expr + "}";
		}

		@Override
		public ETypeSig getType() {
			return expr.getType();
		}

		@Override
		public StrPos getPos() {
			return expr.getPos();
		}

		@Override
		public <T> T match(Function<Child, T> matchChild, Function<ExprList, T> matchExprList,
						   Function<Group, T> matchGroup, Function<Const, T> matchConst,
						   Function<Name, T> matchName, Function<Apply, T> matchApply,
						   Function<ValVar, T> matchValVar, Function<BinOp, T> matchBinOp,
						   Function<Cast, T> matchCast, Function<Custom, T> matchCustom, Function<Lambda, T> matchLambda
		) {
			return matchGroup.apply(this);
		}
	}


	public static class Lambda extends GExpr{

		private final StrPos pos;
		public final PList<TypedName> params;
		public final GExpr code;
		public final ETypeSig returnType;

		public Lambda(StrPos pos, ETypeSig returnType, PList<TypedName> params, GExpr code) {
			this.pos = pos;
			this.params = params;
			this.code = code;
			this.returnType = returnType;
		}

		@Override
		public String toString() {
			return params.toString("(", ", ", ")") + ":" + returnType + " -> { " + code + " }";
		}

		@Override
		public ETypeSig getType() {
			return returnType;
		}

		@Override
		public StrPos getPos() {
			return pos;
		}

		@Override
		public <T> T match(Function<Child, T> matchChild, Function<ExprList, T> matchExprList,
						   Function<Group, T> matchGroup, Function<Const, T> matchConst,
						   Function<Name, T> matchName, Function<Apply, T> matchApply,
						   Function<ValVar, T> matchValVar, Function<BinOp, T> matchBinOp,
						   Function<Cast, T> matchCast, Function<Custom, T> matchCustom, Function<Lambda, T> matchLambda
		) {
			return matchLambda.apply(this);
		}
	}

	public static class Const extends GExpr{

		public final StrPos pos;
		public final ETypeSig type;
		public final Object value;

		public Const(StrPos pos, ETypeSig type, Object value) {
			this.pos = pos;
			this.type = type;
			this.value = value;
		}

		@Override
		public String toString() {
			if(value instanceof String) {
				return "\"" + UString.escapeToJavaString(value.toString()) + "\"" + ":" + type;
			}
			else {
				return "" + value + ":" + type;
			}
		}

		@Override
		public ETypeSig getType() {
			return type;
		}

		@Override
		public StrPos getPos() {
			return pos;
		}

		@Override
		public <T> T match(Function<Child, T> matchChild, Function<ExprList, T> matchExprList,
						   Function<Group, T> matchGroup, Function<Const, T> matchConst,
						   Function<Name, T> matchName, Function<Apply, T> matchApply,
						   Function<ValVar, T> matchValVar, Function<BinOp, T> matchBinOp,
						   Function<Cast, T> matchCast, Function<Custom, T> matchCustom,
						   Function<Lambda, T> matchLambda
		) {
			return matchConst.apply(this);
		}
	}

	public static class Name extends GExpr{

		private final StrPos pos;
		public final String name;
		private final ETypeSig type;

		public Name(StrPos pos, String name, ETypeSig type) {
			this.pos = pos;
			this.name = name;
			this.type = type;
		}


		@Override
		public String toString() {
			return name + ":" + type;
		}

		@Override
		public ETypeSig getType() {
			return type;
		}

		@Override
		public StrPos getPos() {
			return pos;
		}

		@Override
		public <T> T match(Function<Child, T> matchChild, Function<ExprList, T> matchExprList,
						   Function<Group, T> matchGroup, Function<Const, T> matchConst,
						   Function<Name, T> matchName, Function<Apply, T> matchApply,
						   Function<ValVar, T> matchValVar, Function<BinOp, T> matchBinOp,
						   Function<Cast, T> matchCast, Function<Custom, T> matchCustom,
						   Function<Lambda, T> matchLambda
		) {
			return matchName.apply(this);
		}
	}

	public static class Apply extends GExpr{

		private final StrPos pos;
		public final GExpr function;
		public final PList<GExpr> parameters;

		public Apply(StrPos pos, GExpr function,
					 PList<GExpr> parameters
		) {
			this.pos = pos;
			this.function = function;
			this.parameters = parameters;
		}

		@Override
		public String toString() {
			return function + parameters.toString("(", ", ", ")");
		}

		@Override
		public ETypeSig getType() {
			return function.getType();
		}

		@Override
		public StrPos getPos() {
			return pos;
		}

		@Override
		public <T> T match(Function<Child, T> matchChild, Function<ExprList, T> matchExprList,
						   Function<Group, T> matchGroup, Function<Const, T> matchConst,
						   Function<Name, T> matchName, Function<Apply, T> matchApply,
						   Function<ValVar, T> matchValVar, Function<BinOp, T> matchBinOp,
						   Function<Cast, T> matchCast, Function<Custom, T> matchCustom,
						   Function<Lambda, T> matchLambda
		) {
			return matchApply.apply(this);
		}
	}

	public static class ValVar extends GExpr{

		public enum ValVarType{
			val, var
		}

		public final StrPos pos;
		public final TypedName name;
		public final ValVarType valVarType;
		public final GExpr initial;

		public ValVar(StrPos pos, TypedName name,
					  ValVarType valVarType, GExpr initial
		) {
			this.pos = pos;
			this.name = name;
			this.valVarType = valVarType;
			this.initial = initial;
		}

		@Override
		public ETypeSig getType() {
			return name.type;
		}

		@Override
		public StrPos getPos() {
			return pos;
		}

		@Override
		public String toString() {
			return valVarType + " " + name + " = " + initial;
		}

		@Override
		public <T> T match(Function<Child, T> matchChild, Function<ExprList, T> matchExprList,
						   Function<Group, T> matchGroup, Function<Const, T> matchConst,
						   Function<Name, T> matchName, Function<Apply, T> matchApply,
						   Function<ValVar, T> matchValVar, Function<BinOp, T> matchBinOp,
						   Function<Cast, T> matchCast, Function<Custom, T> matchCustom,
						   Function<Lambda, T> matchLambda
		) {
			return matchValVar.apply(this);
		}
	}

	public static class BinOp extends GExpr{

		public final GExpr left;
		public final String op;
		public final GExpr right;

		public BinOp(GExpr left, String op, GExpr right) {
			this.left = left;
			this.op = op;
			this.right = right;
		}

		@Override
		public ETypeSig getType() {
			return left.getType();
		}

		@Override
		public StrPos getPos() {
			return left.getPos();
		}

		@Override
		public String toString() {
			return "" + left + " " + op + " " + right + "";
		}

		@Override
		public <T> T match(Function<Child, T> matchChild, Function<ExprList, T> matchExprList,
						   Function<Group, T> matchGroup, Function<Const, T> matchConst,
						   Function<Name, T> matchName, Function<Apply, T> matchApply,
						   Function<ValVar, T> matchValVar, Function<BinOp, T> matchBinOp,
						   Function<Cast, T> matchCast, Function<Custom, T> matchCustom,
						   Function<Lambda, T> matchLambda
		) {
			return matchBinOp.apply(this);
		}
	}

	public static class Cast extends GExpr{

		public final GExpr left;
		public final ETypeSig typeSig;

		public Cast(GExpr left, ETypeSig typeSig) {
			this.left = left;
			this.typeSig = typeSig;
		}

		@Override
		public ETypeSig getType() {
			return typeSig;
		}

		@Override
		public StrPos getPos() {
			return left.getPos();
		}

		@Override
		public String toString() {
			return "(" + left + ").castTo(" + typeSig + ")";
		}

		@Override
		public <T> T match(Function<Child, T> matchChild, Function<ExprList, T> matchExprList,
						   Function<Group, T> matchGroup, Function<Const, T> matchConst,
						   Function<Name, T> matchName, Function<Apply, T> matchApply,
						   Function<ValVar, T> matchValVar, Function<BinOp, T> matchBinOp,
						   Function<Cast, T> matchCast, Function<Custom, T> matchCustom,
						   Function<Lambda, T> matchLambda
		) {
			return matchCast.apply(this);
		}
	}

	public static GExpr cast(GExpr expr, Class cls) {
		return new Cast(expr, ETypeSig.cls(cls));
	}

	public static class Custom extends GExpr{

		private final StrPos pos;
		public final String name;
		public final PList<Object> arguments;
		private final ETypeSig type;

		public Custom(StrPos pos, String name, PList<Object> arguments, ETypeSig type) {
			this.pos = pos;
			this.name = name;
			this.arguments = arguments;
			this.type = type;
		}

		@Override
		public String toString() {
			return "$" + name + arguments.toString("(", ", ", ")" + ":" + type);
		}

		@Override
		public ETypeSig getType() {
			return type;
		}

		@Override
		public StrPos getPos() {
			return pos;
		}

		@Override
		public <T> T match(Function<Child, T> matchChild, Function<ExprList, T> matchExprList,
						   Function<Group, T> matchGroup, Function<Const, T> matchConst,
						   Function<Name, T> matchName, Function<Apply, T> matchApply,
						   Function<ValVar, T> matchValVar, Function<BinOp, T> matchBinOp,
						   Function<Cast, T> matchCast, Function<Custom, T> matchCustom,
						   Function<Lambda, T> matchLambda
		) {
			return matchCustom.apply(this);
		}
	}
}
