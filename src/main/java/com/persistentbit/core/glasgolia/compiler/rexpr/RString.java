package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.ToDo;

import java.util.function.BiFunction;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public interface RString extends RExpr{

	@Override
	default Class getType() {
		return String.class;
	}

	static RExpr createBinOp(RExpr left, String opString, RExpr right) {
		BiFunction<String, String, Object> op;
		switch(opString) {
			case "+":
				op = (a, b) -> a + b;
				break;
			case "<":
				op = (a, b) -> a.compareTo(b) < 0;
				break;
			case ">":
				op = (a, b) -> a.compareTo(b) > 0;
				break;
			case ">=":
				op = (a, b) -> a.compareTo(b) >= 0;
				break;
			case "<=":
				op = (a, b) -> a.compareTo(b) <= 0;
				break;
			case "==":
				op = (a, b) -> a.equals(b);
				break;
			case "!=":
				op = (a, b) -> a.equals(b) == false;
				break;
			default:
				throw new ToDo("string bin op " + opString);
		}
		return new RString.BinOp(left, opString, op, right);
	}

	class BinOp implements RString{

		private final RExpr left;
		private final RExpr right;
		private final String opString;
		private final BiFunction<String, String, Object> op;

		public BinOp(RExpr left, String opString, BiFunction<String, String, Object> op, RExpr right) {
			this.left = left;
			this.opString = opString;
			this.op = op;
			this.right = right;
		}


		@Override
		public StrPos getPos() {
			return left.getPos();
		}

		@Override
		public Object get() {
			Object leftVal  = left.get();
			Object rightVal = right.get();
			if(leftVal instanceof String == false) {
				throw new EvalException("Not a string:" + leftVal, left.getPos());
			}
			if(rightVal instanceof String == false) {
				rightVal = "" + rightVal;
			}
			return op.apply((String) leftVal, (String) rightVal);
		}

		@Override
		public String toString() {
			return "BinOpString(" + left + " " + opString + " " + right + ")";
		}

		@Override
		public boolean isConst() {
			return left.isConst() && right.isConst();
		}
	}


	class CastToString implements RString{

		public final RExpr expr;

		public CastToString(RExpr expr) {
			this.expr = expr;
		}

		@Override
		public String get() {
			Object value = expr.get();
			if(value == null) {
				return null;
			}
			if(value instanceof String) {
				return (String) value;
			}
			throw new EvalException("Not a String: " + value, expr.getPos());
		}

		@Override
		public StrPos getPos() {
			return expr.getPos();
		}

		@Override
		public boolean isConst() {
			return expr.isConst();
		}
	}
}
