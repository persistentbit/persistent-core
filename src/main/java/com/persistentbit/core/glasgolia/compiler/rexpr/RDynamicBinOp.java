package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.exceptions.ToDo;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UNumber;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/03/17
 */
public class RDynamicBinOp implements RExpr{

	private final RExpr left;
	private final String op;
	private final RExpr right;

	public RDynamicBinOp(RExpr left, String op, RExpr right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	@Override
	public Class getType() {
		return left.getType();
	}

	@Override
	public StrPos getPos() {
		return left.getPos();
	}

	@Override
	public boolean isConst() {
		return left.isConst() && right.isConst();
	}

	@Override
	public Object get() {
		Object valLeft  = left.get();
		Object valRight = right.get();
		if(valLeft == null) {
			switch(op){
				case "==": return valRight == null;
				case "!=": return valRight != null;
				case "<=": return valRight == null;
				case "<": return valRight != null;
				case ">=": return valRight == null;
				case ">": return false;
			}
			throw new EvalException("Can't execute binary operator '" + op + "' on null", getPos());
		}
		if(valLeft instanceof Number) {
			Tuple2<Number, Number> tlr = UNumber.unify((Number) valLeft, (Number) valRight).orElseThrow();
			Number                 l   = tlr._1;
			Number                 r   = tlr._2;
			if(l instanceof Integer) {
				return RInteger.getOperatorFunction(op).apply((Integer) l, (Integer) r);
			}
			if(l instanceof Double){
				return RDouble.getOperatorFunction(op).apply((Double)l, (Double)r);
			}
			if(l instanceof Long) {
				return RLong.getOperatorFunction(op).apply((Long) l, (Long) r);
			}
		}
		if(valLeft instanceof Boolean) {
			return RBoolean.getOperatorFunction(op).apply((Boolean) valLeft, (Boolean) valRight);
		}
		if(valLeft instanceof String) {
			return RString.getOperatorFunction(op).apply((String) valLeft, String.valueOf(valRight));
		}
		switch (op){
			case "==": return valLeft.equals(valRight);
			case "!=": return valLeft.equals(valRight) == false;
		}
		if(valLeft instanceof Comparable){
			Comparable c = (Comparable)valLeft;
			int res = ((Comparable) valLeft).compareTo(valRight);
			switch(op){
				case "<": return res < 0;
				case "<=": return res <= 0;
				case ">": return res > 0;
				case ">=": return res >=0;
			}
		}
		throw new ToDo("bin op for " + valLeft);
	}

	@Override
	public String toString() {
		return "RJavaBinOp(" + left + " " + op + " " + right + ")";
	}


}
