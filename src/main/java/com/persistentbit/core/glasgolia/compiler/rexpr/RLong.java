package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.ToDo;
import com.persistentbit.core.utils.UNumber;

import java.util.function.BiFunction;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public interface RLong extends RExpr{

	@Override
	default Class getType() {
		return Long.class;
	}

	static RExpr createBinOp(RExpr left, String opString, RExpr right) {
		BiFunction<Long, Long, Object> op = getOperatorFunction(opString);
		return new BinOp(left, opString, op, right);
	}

	static BiFunction<Long, Long, Object> getOperatorFunction(String opString) {
		BiFunction<Long, Long, Object> op;
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
				throw new ToDo("long bin op " + opString);
		}
		return op;
	}

	class BinOp implements RLong{

		private final RExpr left;
		private final RExpr right;
		private final String opString;
		private final BiFunction<Long, Long, Object> op;

		public BinOp(RExpr left, String opString, BiFunction<Long, Long, Object> op, RExpr right) {
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
			if(leftVal instanceof Long == false) {
				throw new EvalException("Not a long:" + leftVal, left.getPos());
			}
			if(rightVal instanceof Long == false) {
				throw new EvalException("Not a long:" + rightVal, right.getPos());
			}
			return op.apply((Long) leftVal, (Long) rightVal);
		}

		@Override
		public String toString() {
			return "BinOpLong(" + left + " " + opString + " " + right + ")";
		}

		@Override
		public boolean isConst() {
			return left.isConst() && right.isConst();
		}
	}


	class CastToLong implements RLong{

		public final RExpr expr;

		public CastToLong(RExpr expr) {
			this.expr = expr;
		}

		@Override
		public Long get() {
			Object value = expr.get();
			if(value == null) {
				return null;
			}
			if(value instanceof Long) {
				return (Long) value;
			}
			if(value instanceof Number) {
				return UNumber.convertToLong((Number) value)
							  .orElseThrow(() -> new EvalException("Not a long: " + value, expr.getPos()));
			}
			throw new EvalException("Not an int: " + value, expr.getPos());
		}

		@Override
		public StrPos getPos() {
			return expr.getPos();
		}

		@Override
		public String toString() {
			return "ToLong(" + expr + ")";
		}

		@Override
		public boolean isConst() {
			return expr.isConst();
		}
	}
}
