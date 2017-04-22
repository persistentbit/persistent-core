package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.exceptions.ToDo;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UNumber;

import java.util.function.BiFunction;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public interface RInteger extends RExpr{

	@Override
	default Class getType() {
		return Integer.class;
	}

	static RExpr createBinOp(RExpr left, String opString, RExpr right) {
		BiFunction<Integer, Integer, Object> op = getOperatorFunction(opString);
		return new BinOp(left, opString, op, right);
	}

	static BiFunction<Integer, Integer, Object> getOperatorFunction(String opString) {
		BiFunction<Integer, Integer, Object> op;
		switch(opString) {
			case "+":
				op = (a, b) -> a + b;
				break;
			case "-":
				op = (a, b) -> a - b;
				break;
			case "/":
				op = (a, b) -> a / b;
				break;
			case "*":
				op = (a, b) -> a * b;
				break;
			case "<":
				op = (a, b) -> a < b;
				break;
			case ">":
				op = (a, b) -> a > b;
				break;
			case ">=":
				op = (a, b) -> a >= b;
				break;
			case "<=":
				op = (a, b) -> a <= b;
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

	class BinOp implements RInteger{

		private final RExpr left;
		private final RExpr right;
		private final String opString;
		private final BiFunction<Integer, Integer, Object> op;

		public BinOp(RExpr left, String opString, BiFunction<Integer, Integer, Object> op, RExpr right) {
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
			if(leftVal instanceof Integer == false) {
				throw new EvalException("Not an int:" + leftVal, left.getPos());
			}
			if(rightVal instanceof Integer == false) {
				throw new EvalException("Not an int:" + rightVal, right.getPos());
			}
			return op.apply((Integer) leftVal, (Integer) rightVal);
		}

		@Override
		public String toString() {
			return "BinOpInt(" + left + " " + opString + " " + right + ")";
		}

		@Override
		public boolean isConst() {
			return left.isConst() && right.isConst();
		}
	}


	class CastToInt implements RInteger{

		public final RExpr expr;

		public CastToInt(RExpr expr) {
			this.expr = expr;
		}

		@Override
		public Integer get() {
			Object value = expr.get();
			if(value == null) {
				return null;
			}
			if(value instanceof Integer) {
				return (Integer) value;
			}
			if(value instanceof Number) {
				return UNumber.convertToInteger((Number) value)
							  .orElseThrow(() -> new EvalException("Not an int: " + value, expr.getPos()));
			}
			throw new EvalException("Not an int: " + value, expr.getPos());
		}

		@Override
		public StrPos getPos() {
			return expr.getPos();
		}

		@Override
		public String toString() {
			return "ToInt(" + expr + ")";
		}

		@Override
		public boolean isConst() {
			return expr.isConst();
		}
	}
}
