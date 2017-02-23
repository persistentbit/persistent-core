package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.parser.Scan;
import com.persistentbit.core.parser.WithPos;
import com.persistentbit.core.utils.StrPos;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/02/17
 */
public class EParser{

	public static Parser<String> lineComment = Scan.lineComment("//");
	public static Parser<String> blockComment = Scan.blockComment("/*", "*?");


	public static Parser<String> ws =
		Scan.parseWhiteSpaceWithComment(Scan.whiteSpaceAndNewLine, lineComment.or(blockComment));

	public static Parser<String> keyword(String keyword) {
		return Scan.keyword(keyword).skip(ws);
	}

	public static Parser<String> term(String term) {
		return Scan.term(term).skip(ws);
	}



	public static Parser<PList<EExpr>> parseApply() {
		return source ->
			term("(")
				.skipAnd(Parser.zeroOrMoreSep(parseExpr(), term(",")))
				.skip(ws)
				.skip(term(")"))
				.parse(source);
	}

	private static Parser<String> orTerms(String... terms) {
		Parser<String> res = term(terms[0]);
		for(int t = 1; t < terms.length; t++) {
			res = res.or(term(terms[t]));
		}
		return res;
	}


	public static Parser<String> operatorShiftL6 = orTerms(">>>", "<<", ">>");
	public static Parser<String> operatorComparisonL7 = orTerms("<=", ">=", "<", ">");
	public static Parser<String> operatorEqualityL8 = orTerms("==", "!=");
	public static Parser<String> operatorBitwiseANDL9 = term("&");
	public static Parser<String> operatorBitwiseXORL10 = term("^");
	public static Parser<String> operatorBitwiseORL11 = term("|");
	public static Parser<String> operatorCondANDL12 = term("&&");
	public static Parser<String> operatorCondORL13 = term("||");
	//public static Parser<String> operatorConditionalL14 = term("?:");
	//public static Parser<String> operatorAssignmentL15 = ....


	public static Parser<EExpr> parseFactorExpr = source -> Log.function().code(l -> Parser.orOf(
		term("(")
			.skipAnd(parseExpr())
			.skip(term(")"))
			.withPos()
			.map(e -> new EExpr.Group(e.pos, e.value, EExpr.Group.Type.group)),
		parseName(),
		parseConst()
	).onErrorAddMessage("Expected a Variable or a literal or (<expr>)!")
																						   .skip(ws)
																						   .onErrorAddMessage("Expected an expression factor")
																						   .and(parseApply().withPos()
																											.optional())
																						   .map(t ->
																							   t._2.<EExpr>map(args -> new EExpr.Apply(args.pos, t._1, args.value))
																								   .orElse(t._1)
																						   )
																						   .parse(source));

	public static Parser<EExpr> parseTermExpr = source -> {
		return parseBinOp(
			parseFactorExpr,
			Parser.orOf(
				term("*"), term("/"), term("and")
			).onErrorAddMessage("Expected an expression term operator").skip(ws),
			parseFactorExpr
		)
			.skip(ws)
			.onErrorAddMessage("Expected an expression term")
			.parse(source);
	};


	public static Parser<EExpr> parsePrimitiveL0 =
		source ->
			term("(")
				.skipAnd(parseExpr())
				.skip(term(")"))
				.withPos()
				.<EExpr>map(e -> new EExpr.Group(e.pos, e.value, EExpr.Group.Type.group))
				.or(parseName())
				.or(parseConst())
				.onErrorAddMessage("Expected an expression primitive").parse(source);

	private static EExpr createApply(EExpr left, String name, EExpr right) {
		EExpr.Child function = new EExpr.Child(left.pos, left, name);
		return new EExpr.Apply(left.pos, function, PList.val(right));
	}


	public static Parser<EExpr> parseArrayAccess(EExpr left) {
		return
			term("[").skipAnd(parseExpr()).skip(term("]"))
					 .map(arr -> createApply(left, "[]", arr));


	}

	public static Parser<EExpr> parseChild(EExpr left) {
		return
			term(".").skipAnd(parseNameString())
					 .map(nameWithPos -> new EExpr.Child(nameWithPos.pos, left, nameWithPos.value));
	}

	public static Parser<EExpr> parseApply(EExpr left) {
		return term("(")
			.skipAnd(Parser.zeroOrMoreSep(parseExpr(), term(",")).withPos())
			.skip(ws)
			.skip(term(")"))
			.map(argList -> new EExpr.Apply(argList.pos, left, argList.value));
	}

	public static Parser<EExpr> parsePostL1() {
		return source -> {

			ParseResult<EExpr> left = parsePrimitiveL0.parse(source);
			if(left.isFailure()) {
				return left;
			}


			while(true) {
				EExpr leftExpr = left.getValue();
				ParseResult<EExpr> newLeft =
					parseArrayAccess(leftExpr)
						.or(parseChild(leftExpr))
						.parse(left.getSource());

				if(newLeft.isFailure()) {
					return left;
				}
				left = newLeft;
			}
		};
	}

	public static Parser<EExpr> parsePreUnaryL2 = parsePostL1();

		/*orTerms("++_","--_","+_","-_","!_","~_")
			.withPos()
				.and(parsePostL1())
				.map(t -> createApply(t._2,t._1.value,new EExpr.Const(t._1.pos,null)))
				.onErrorAddMessage("Expected an prefix unary operator.")
				.or(parsePostL1())
		;
		/*orTerms("++_","--_","+_","-_","!_","~_").withPos()
		.and(parsePostL1())
		.map(t -> createApply(t._2,t._1.value,new EExpr.Const(t._1.pos,null)))
		.onErrorAddMessage("Expected an prefix unary operator.")
		.or(parsePostL1())
		;*/

	public static Parser<EExpr> parseCastAndNewL3 = parsePreUnaryL2;

	public static Parser<EExpr> parseBinOpMultiplicativeL4 =
		parseBinOp(parseCastAndNewL3, orTerms("*", "/", "%"), parseCastAndNewL3)
			.skip(ws)
			.onErrorAddMessage("Expected a multiplicative operator.");

	public static Parser<EExpr> parseBinOpAdditveL5 =
		parseBinOp(parseBinOpMultiplicativeL4, orTerms("+", "-"), parseBinOpMultiplicativeL4)
			.skip(ws)
			.onErrorAddMessage("Expected an additive operator.");

	public static Parser<EExpr> parseSimpleExpr = parseBinOpAdditveL5; /*source -> {
		Parser<EExpr> parser = parseBinOp(
			parseTermExpr,
			Parser.orOf(term("+"), term("-"), term("or"))
				  .onErrorAddMessage("Expected a term operator")
				  .skip(ws),
			parseTermExpr.skip(ws)
		).skip(ws);
		return parser.parse(source);
	};*/

	public static Parser<EExpr> parseValExpr() {
		return keyword("val")
			.skipAnd(parseNameString())
			.skip(term("="))
			.and(parseExpr())
			.map(t -> new EExpr.Val(t._1.pos, t._1.value, t._2));
	}

	public static Parser<PList<String>> nameList =
		term("(")
			.skipAnd(Parser.zeroOrMoreSep(Scan.identifier.skip(ws), term(",")))
			.skip(term(")"))
			.onErrorAddMessage("Expected name list");


	public static Parser<EExpr> parseExpr() {
		return source ->
			parseValExpr()
				.or(parseSimpleExpr)
				.skip(ws).parse(source);
	}


	public static Parser<EExpr> parseBinOp(Parser<EExpr> left, Parser<String> op, Parser<EExpr> right) {
		return source -> {
			ParseResult<EExpr> leftRes = left.parse(source);
			if(leftRes.isFailure()) {
				return leftRes;
			}
			while(true) {
				ParseResult<String> opRes = op.parse(leftRes.getSource());
				if(opRes.isFailure()) {
					//return opRes.map(v -> null);
					return leftRes;
				}
				/*String opResValue = opRes.getValue();
				if(opResValue == null) {
					//System.out.println(leftRes);
					return leftRes.map(v -> v.value);
				}*/
				ParseResult<EExpr> rightRes = right.parse(opRes.getSource());
				if(rightRes.isFailure()) {
					return rightRes;
				}
				StrPos leftPos = leftRes.getValue().pos;
				leftRes = ParseResult.success(
					rightRes.getSource(),
					new EExpr.BinOp(leftPos, leftRes.getValue(), opRes.getValue(), rightRes.getValue())
				);
			}

		};
	}

	public static Parser<WithPos<String>> parseNameString() {
		return source ->
			Scan.identifier
				.withPos()
				.skip(ws)
				.onErrorAddMessage("Expected a variable name")
				.parse(source)
			;
	}

	public static Parser<EExpr> parseName() {
		return source ->
			parseNameString()
				.map(name -> new EExpr.Name(name.pos, name.value))
				.onErrorAddMessage("Expected a variable name")
				.and(term("->").skipAnd(parseExpr()).optional())
				.map(t ->
					t._2.<EExpr>map(code -> new EExpr.Lambda(t._1.pos, t._1.name, code)).orElse(t._1)
				)
				.parse(source)
			;
	}

	public static Parser<EExpr> parseStringLiteral =
		Scan.stringLiteral("\"\"\"", true)
			.or(Scan.stringLiteral("\'", false))
			.or(Scan.stringLiteral("\"", false))
			.skip(ws)
			.withPos()
			.map(value -> new EExpr.Const(value.pos, value.value));

	public static Parser<EExpr> parseNumberLiteral =
		Scan.doubleLiteral.withPos().<EExpr>map(v -> new EExpr.Const(v.pos, v.value))
			.or(Scan.integerLiteral.withPos().<EExpr>map(v -> new EExpr.Const(v.pos, v.value)))
			.skip(ws);

	public static Parser<EExpr> parseBooleanLiteral =
		keyword("true").withPos().<EExpr>map(s -> new EExpr.Const(s.pos, true))
			.or(keyword("false").withPos().<EExpr>map(s -> new EExpr.Const(s.pos, false)))
			.onErrorAddMessage("Expected 'true' or 'false'");

	public static Parser<EExpr> parseConst() {
		return parseNumberLiteral
			.or(parseBooleanLiteral)
			.or(parseStringLiteral)
			.onErrorAddMessage("Expected a literal!");
	}


}
