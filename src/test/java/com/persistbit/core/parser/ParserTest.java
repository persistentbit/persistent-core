package com.persistbit.core.parser;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.parser.Scan;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 17/02/17
 */
public class ParserTest{

	interface Expr{

	}

	static class GroupExpr implements Expr{

		private final Expr expr;

		public GroupExpr(Expr expr) {
			this.expr = expr;
		}

		@Override
		public String toString() {
			return "(" + expr + ")";
		}
	}

	static class ConstExpr implements Expr{

		private final Object value;

		public ConstExpr(Object value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value.toString();
		}
	}

	static class VarExpr implements Expr{

		private final String varName;

		public VarExpr(String varName) {
			this.varName = varName;
		}

		@Override
		public String toString() {
			return varName;
		}
	}

	static class BinOpExpr implements Expr{

		private final Expr   left;
		private final String binOp;
		private final Expr   right;

		public BinOpExpr(Expr left, String binOp, Expr right) {
			this.left = left;
			this.binOp = binOp;
			this.right = right;
		}

		@Override
		public String toString() {
			return left + " " + binOp + " " + right;
		}
	}


	public static Parser<Expr> parseFactorExpr = source -> Log.function().code(l -> {
		return
			Parser.or(
					  Scan.term("(")
						  .skipAndThen(parseExpr())
						  .andThenSkip(Scan.term(")"))
						  .map(e -> (Expr) new GroupExpr(e)),
					  parseVar(),
					  parseConst()
			).onErrorAddMessage("Expected a Variable or a literal or (<expr>)!")
				  .skipWhiteSpace()
				  .parse(source);
	});

	public static Parser<Expr> parseTermExpr   = source -> {
		return parseBinOp(
			parseFactorExpr,
			Parser.or(
					  Scan.term("*"), Scan.term("/"), Scan.term("and")
			).onErrorAddMessage("Expected a expression term operator"),
			parseFactorExpr
		).skipWhiteSpace().parse(source);
	};
	public static Parser<Expr> parseSimpleExpr = source -> {
		Parser<Expr> parser = parseBinOp(
			parseTermExpr,
			Parser.or(Scan.term("+"), Scan.term("-"), Scan.term("or"))
				  .onErrorAddMessage("Expectedd a term operator")
				  .skipWhiteSpace(),
			parseTermExpr
		).skipWhiteSpace();
		return parser.parse(source);
	};

	public static Parser<Expr> parseExpr() {
		return parseSimpleExpr;
	}


	public static Parser<Expr> parseBinOp(Parser<Expr> left, Parser<String> op, Parser<Expr> right) {
		Parser<Expr> parser = source -> {
			ParseResult<Expr> leftRes = left.parse(source);
			if(leftRes.isFailure()) {
				return leftRes;
			}
			while(true) {
				ParseResult<Optional<String>> opRes = op.optional().parse(leftRes.getSource());
				if(opRes.isFailure()) {
					return opRes.map(v -> null);
				}
				String opResValue = opRes.getValue().orElse(null);
				if(opResValue == null) {
					return leftRes;
				}
				ParseResult<Expr> rightRes = right.parse(opRes.getSource());
				if(rightRes.isFailure()) {
					return rightRes;
				}
				leftRes = ParseResult
					.success(rightRes.getSource(), new BinOpExpr(leftRes.getValue(), opResValue, rightRes.getValue()));
			}
		};
		return parser;
		/*return left.andThen(
			op.andThen(right).optional()
		).map(t -> {
			if(t._2.isPresent() == false){
				return t._1;
			}
			return new BinOpExpr(t._1,t._2.get()._1,t._2.get()._2);
		});*/
	}


	public static Parser<Expr> parseVar() {
		return Scan.identifier.map(name -> new VarExpr(name));
	}

	public static Parser<Expr> parseConst() {
		return Scan.integerLiteral.map(value -> new ConstExpr(value));
	}


	static final TestCase simpleExpr = TestCase.name("parse simple expression").code(tr -> {
		String source = "(1+2)*varName/3-4+(1234/1*0)-name";
		tr.info(parseExpr().andThenEof().parse(Source.asSource("test", source)).getValue());
	});

	public void testAll() {
		TestRunner.runAndPrint(LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)), ParserTest.class);
	}

	public static void main(String[] args) {
		LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();
		new ParserTest().testAll();
	}
}
