package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.PList;
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

	public static final Parser<EExpr> parseName = parseNameString().map(name -> new EExpr.Name(name.pos, name.value));

	/*public static Parser<PList<EExpr>> parseApply() {
		return source ->
			term("(")
				.skipAnd(Parser.zeroOrMoreSep(parseExpr(), term(",")))
				.skip(term(")"))
				.parse(source);
	}*/

	private static Parser<String> orTerms(String... terms) {
		Parser<String> res = term(terms[0]);
		for(int t = 1; t < terms.length; t++) {
			res = res.or(term(terms[t]));
		}
		return res;
	}



	//public static Parser<String> operatorConditionalL14 = term("?:");
	//public static Parser<String> operatorAssignmentL15 = ....



	public static Parser<EExpr> parseIf() {
		return
			keyword("if")
			.skipAnd(parseExpr().and(parseExpr()))
			.and(
				keyword("else")
				.skipAnd(parseExpr())
				.optional()
			)
			.map(t -> {
				if(t._2.isPresent()){
					return new EExpr.Custom(
							t._1._1.pos,
							"ifElse",
							PList.val(t._1._1,t._1._2,t._2.get())
					);
				} else {
					return new EExpr.Custom(
							t._1._1.pos,
							"if",
							PList.val(t._1._1,t._1._2)
					);
				}
			});
	}

	public static Parser<EExpr> parseImport() {
		return
			keyword("import")
				.skipAnd(parseStringLiteral.withPos())
				.and(keyword("as").skipAnd(parseName).optional())
				.map(t -> {
					return new EExpr.Custom(
						t._1.pos,
						"import",
						PList.val(t._1.value, t._2.orElse(null))
					);
				});
	}

	public static Parser<EExpr> parseWhile() {
		return
			keyword("while")
				.skipAnd(parseExpr())
				.and(parseExpr())
				.map(t ->
					new EExpr.Custom(
						t._1.pos,
						"while",
						PList.val(t._1, t._2)
					)
				);
	}

	public static Parser<EExpr> parseSource() {
		return
			keyword("source")
				.skipAnd(parseStringLiteral)
				.map(t -> new EExpr.Custom(
					t.pos,
					"source",
					PList.val(t)
				));
	}

	public static Parser<EExpr> parseClasspath() {
		return
			keyword("classpath")
				.skipAnd(parseStringLiteral)
				.map(t -> new EExpr.Custom(
					t.pos,
					"classpath",
					PList.val(t)
				));
	}

	public static Parser<EExpr> parseCustom() {
		return Parser.orOf(parseIf(), parseImport(), parseWhile(), parseSource(), parseClasspath());
	}

	public static Parser<EExpr> parsePrimitiveL0 =
		source ->
				parseConst()
					.or(parseLambda())
					.or(term("(").skipAnd(parseExpr()).skip(term(")")).withPos()
					.<EExpr>map(e -> new EExpr.Group(e.pos, e.value, EExpr.Group.Type.group))
				)
					.or(parseExprBlock(EExpr.Group.Type.block))
					.or(parseExprBlock(EExpr.Group.Type.group))
					.or(parseCustom())
					.or(parseName)
					.onErrorAddMessage("Expected an expression primitive")
					.skip(ws)
					.onErrorAddMessage("Expected an expression factor")
				/*.and(parseApply().withPos()
								 .optional())
				.map(t ->
					t._2.<EExpr>map(args -> new EExpr.Apply(args.pos, t._1, args.value))
						.orElse(t._1)
				)*/
					.parse(source);

	public static Parser<EExpr> parseLambda() {
		return source -> {
			return term("(").skipAnd(Parser.zeroOrMoreSep(parseNameString(), term(",")).skip(term(")")))
							.or(parseNameString().map(name -> PList.val(name)))
							.skip(term("->")).and(parseExpr())
				.<EExpr>map(t -> new EExpr.Lambda(source.position, t._1.map(wp -> wp.value), t._2))
				.parse(source);
		};
	}


	private static EExpr createApply(EExpr left, StrPos namePos, String name, EExpr right) {
		EExpr.Child function = new EExpr.Child(left.pos, left, name);
		return new EExpr.Apply(left.pos, function, PList.val(right));
	}


	public static Parser<EExpr> parseArrayAccess(EExpr left) {
		return
			term("[").skipAnd(parseExpr()).skip(term("]"))
					 .map(arr -> createApply(left, left.pos, "[]", arr));


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
						.or(parseApply(leftExpr))
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






	public static Parser<EExpr> parseBinOpShiftL6 =
			parseBinOp(parseBinOpAdditveL5, orTerms(">>>", "<<", ">>"), parseBinOpAdditveL5)
					.skip(ws)
					.onErrorAddMessage("Expected a shift operator ");

	public static Parser<EExpr> parseBinOpComparisonL7 =
			parseBinOp(parseBinOpShiftL6, orTerms("<=", ">=", "<", ">"), parseBinOpShiftL6)
					.skip(ws)
					.onErrorAddMessage("Expected a comparison operator.");

	public static Parser<EExpr> parseBinOpEqualityL8 =
			parseBinOp(parseBinOpComparisonL7, orTerms("==", "!="), parseBinOpComparisonL7)
					.skip(ws)
					.onErrorAddMessage("Expected an equality operator.");

	public static Parser<EExpr> parseBinOpBitwiseANDL9 =
			parseBinOp(parseBinOpEqualityL8, term("&").and(Parser.not("",term("&"))).map(t -> t._1), parseBinOpEqualityL8)
					.skip(ws)
					.onErrorAddMessage("Expected a bitwise and operator.");

	public static Parser<EExpr> parseBinOpBitwiseXORL10 =
			parseBinOp(parseBinOpBitwiseANDL9, term("^"), parseBinOpBitwiseANDL9)
					.skip(ws)
					.onErrorAddMessage("Expected a bitwise and operator.");

	public static Parser<EExpr> parseBinOpBitwiseORL11 =
			parseBinOp(parseBinOpBitwiseXORL10, term("|").and(Parser.not("",term("|"))).map(t -> t._1), parseBinOpBitwiseXORL10)
					.skip(ws)
					.onErrorAddMessage("Expected a bitwise and operator.");


	public static Parser<EExpr> parseBinOpCondANDL12 =
			parseBinOp(parseBinOpBitwiseORL11, term("&&"), parseBinOpBitwiseORL11)
					.skip(ws)
					.onErrorAddMessage("Expected a bitwise and operator.");



	public static Parser<EExpr> parseBinOpCondORL13 =
			parseBinOp(parseBinOpCondANDL12, term("||"), parseBinOpCondANDL12)
					.skip(ws)
					.onErrorAddMessage("Expected a bitwise and operator.");






	public static Parser<EExpr> parseSimpleExpr = parseBinOpCondORL13; /*source -> {
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
		return keyword("val").map(v -> EExpr.Val.Type.val)
							 .or(keyword("var").map(v -> EExpr.Val.Type.var))
							 .or(Scan.whiteSpace.map(v -> EExpr.Val.Type.assign))
							 .and(parseNameString())
							 .skip(term("="))
							 .and(parseExpr())
							 .map(t -> new EExpr.Val(t._1._2.pos, t._1._2.value, t._2, t._1._1));
	}

	public static Parser<PList<String>> nameList =
		term("(")
			.skipAnd(Parser.zeroOrMoreSep(Scan.identifier.skip(ws), term(",")))
			.skip(term(")"))
			.onErrorAddMessage("Expected name list");


	public static Parser<EExpr> parseExprBlock(EExpr.Group.Type blockType) {
		String left;
		String right;
		switch(blockType) {
			case block:
				left = "{";
				right = "}";
				break;
			case group:
				left = "(";
				right = ")";
				break;
			default:
				throw new RuntimeException("Unknown type: " + blockType);
		}
		return
			term(left).onlyPos()
					  .and(parseExprList())
					  .skip(term(right))
					  .map(t -> new EExpr.Group(t._1, t._2, blockType));
	}

	public static Parser<EExpr> parseExpr() {
		return source ->
			parseValExpr()
				.or(parseSimpleExpr)
				.skip(ws).parse(source);
	}

	public static final Parser<String> parseEndOfExpression =
		term(";");

	public static Parser<EExpr> parseExprList() {
		return source -> {
			StrPos       startPos = source.position;
			PList<EExpr> result   = PList.empty();
			while(true) {
				ParseResult<EExpr> exprResult = parseExpr().parse(source);
				if(exprResult.isFailure()) {
					break;
				}
				result = result.plus(exprResult.getValue());
				source = exprResult.getSource();

				ParseResult<String> endOfExprREsult = parseEndOfExpression.parse(source);
				if(endOfExprREsult.isFailure()) {
					break;
				}
				source = endOfExprREsult.getSource();
			}
			//if(result.isEmpty()){
			//	return ParseResult.failure(source,"Not a valid Expression");
			//}

			return ParseResult.success(source,
				new EExpr.ExprList(startPos, result.toImmutableArray())
			);
		};
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

				EExpr result =
					createApply(leftRes.getValue(), opRes.getSource().position, opRes.getValue(), rightRes.getValue());

				leftRes = ParseResult.success(rightRes.getSource(), result);
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
			.or(keyword("null").withPos().map(v -> new EExpr.Const(v.pos,null)))
			.onErrorAddMessage("Expected a literal!");
	}


}
