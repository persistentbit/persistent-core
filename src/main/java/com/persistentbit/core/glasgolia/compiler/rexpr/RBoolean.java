package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.exceptions.ToDo;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.utils.StrPos;

import java.util.function.BiFunction;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public interface RBoolean extends RExpr{

	@Override
	default Class getType() {
		return Boolean.class;
	}

	static RExpr createBinOp(RExpr left, String opString, RExpr right) {
		BiFunction<Boolean, Boolean, Object> op = getOperatorFunction(opString);
		return new BinOp(left, opString, op, right);
	}

	static BiFunction<Boolean, Boolean, Object> getOperatorFunction(String opString) {
		BiFunction<Boolean, Boolean, Object> op;
		switch(opString) {
			case "||":
				op = (a, b) -> a || b;
				break;
			case "&&":
				op = (a, b) -> a && b;
				break;
			case "==":
				op = (a, b) -> a.equals(b);
				break;
			case "!=":
				op = (a, b) -> a.equals(b) == false;
				break;
			default:
				throw new ToDo("int bin op " + opString);
		}
		return op;
	}

	class BinOp implements RBoolean{

		private final RExpr left;
		private final RExpr right;
		private final String opString;
		private final BiFunction<Boolean, Boolean, Object> op;

		public BinOp(RExpr left, String opString, BiFunction<Boolean, Boolean, Object> op, RExpr right) {
			this.left = left;
			this.opString = opString;
			this.op = op;
			this.right = right;
		}

		@Override
		public boolean isConst() {
			return left.isConst() && right.isConst();
		}

		@Override
		public StrPos getPos() {
			return left.getPos();
		}

		@Override
		public Object get() {
			Object leftVal = left.get();
			if(leftVal instanceof Boolean == false) {
				throw new EvalException("Not a boolean:" + leftVal, left.getPos());
			}
			if(((Boolean) leftVal) == false && opString.equals("||")) {
				return false;
			}

			Object rightVal = right.get();
			if(rightVal instanceof Integer == false) {
				throw new EvalException("Not a boolean:" + rightVal, right.getPos());
			}
			return op.apply((Boolean) leftVal, (Boolean) rightVal);
		}

		@Override
		public String toString() {
			return "BinOpBoolean(" + left + " " + opString + " " + right + ")";
		}
	}


	class CastToBoolean implements RBoolean{

		public final RExpr expr;

		public CastToBoolean(RExpr expr) {
			this.expr = expr;
		}

		@Override
		public Boolean get() {
			Object value = expr.get();
			if(value == null) {
				return null;
			}
			if(value instanceof Boolean) {
				return (Boolean) value;
			}

			throw new EvalException("Not a boolean: " + value, expr.getPos());
		}

		@Override
		public boolean isConst() {
			return expr.isConst();
		}

		@Override
		public StrPos getPos() {
			return expr.getPos();
		}
	}
}
