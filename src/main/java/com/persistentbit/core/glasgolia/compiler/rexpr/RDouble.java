package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.exceptions.ToDo;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UNumber;

import java.util.function.BiFunction;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/03/2017
 */
public interface RDouble extends RExpr{

    @Override
    default Class getType() {
        return Double.class;
    }

    static RExpr createBinOp(RExpr left, String opString, RExpr right) {
        BiFunction<Double, Double, Object> op = getOperatorFunction(opString);
        return new BinOp(left, opString, op, right);
    }

    static BiFunction<Double, Double, Object> getOperatorFunction(String opString) {
        BiFunction<Double, Double, Object> op;
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

    class BinOp implements RDouble{

        private final RExpr left;
        private final RExpr right;
        private final String opString;
        private final BiFunction<Double, Double, Object> op;

        public BinOp(RExpr left, String opString, BiFunction<Double, Double, Object> op, RExpr right) {
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
            if(leftVal instanceof Double == false) {
                throw new EvalException("Not a double:" + leftVal, left.getPos());
            }
            if(rightVal instanceof Double == false) {
                throw new EvalException("Not a double:" + rightVal, right.getPos());
            }
            return op.apply((Double) leftVal, (Double) rightVal);
        }

        @Override
        public String toString() {
            return "BinOpDouble(" + left + " " + opString + " " + right + ")";
        }

        @Override
        public boolean isConst() {
            return left.isConst() && right.isConst();
        }
    }


    class CastToDouble implements RDouble{

        public final RExpr expr;

        public CastToDouble(RExpr expr) {
            this.expr = expr;
        }

        @Override
        public Double get() {
            Object value = expr.get();
            if(value == null) {
                return null;
            }
            if(value instanceof Double) {
                return (Double) value;
            }
            if(value instanceof Number) {
                return UNumber.convertToDouble((Number) value)
                        .orElseThrow(() -> new EvalException("Not a double: " + value, expr.getPos()));
            }
            throw new EvalException("Not a double: " + value, expr.getPos());
        }

        @Override
        public StrPos getPos() {
            return expr.getPos();
        }

        @Override
        public String toString() {
            return "ToDouble(" + expr + ")";
        }

        @Override
        public boolean isConst() {
            return expr.isConst();
        }
    }
}
