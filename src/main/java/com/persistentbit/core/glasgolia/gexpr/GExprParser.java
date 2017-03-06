package com.persistentbit.core.glasgolia.gexpr;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.glasgolia.ETypeSig;
import com.persistentbit.core.glasgolia.ETypeSigParser;
import com.persistentbit.core.glasgolia.gexpr.custom.GExprStatementParsers;
import com.persistentbit.core.parser.*;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UNumber;

import java.util.function.Supplier;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/03/17
 */
public class GExprParser{

	private final Parser<String> lineComment = Scan.lineComment("//");
	private final Parser<String> blockComment = Scan.blockComment("/*", "*?");
	public final Parser<String> ws =
		Scan.parseWhiteSpaceWithComment(Scan.whiteSpaceAndNewLine, lineComment.or(blockComment));

	public final ETypeSigParser typeSigParser = new ETypeSigParser(ws);

	private final Supplier<Parser<GExpr>> customParsers;


	public GExprParser(GExprCustomParser customParsers) {
		this.customParsers = () -> customParsers.getParser(this);
	}

	public GExprParser() {
		this(new GExprStatementParsers());
	}

	public Parser<String> eof(){
		return ws.andEof();
	}

	public Parser<String> keyword(String keyword) {
		return Scan.keyword(keyword).skip(ws);
	}

	private Parser<String> term(String term) {
		return Scan.term(term).skip(ws);
	}

	public Parser<GExpr> parseName = parseNameString().map(name -> new GExpr.Name(name.pos, name.value, ETypeSig.any));

	public Parser<String> orTerms(String... terms) {
		Parser<String> res = term(terms[0]);
		for(int t = 1; t < terms.length; t++) {
			res = res.or(term(terms[t]));
		}
		return res;
	}
	private Parser<GExpr> parseCustom() {
		return customParsers.get();

	}

	private Parser<GExpr> parsePrimitiveL0 =
		source ->
			parseConst()
				.or(parseLambda())
				.or(term("(").skipAnd(parseExpr()).skip(term(")")).withPos()
					.<GExpr>map(e -> new GExpr.Group(e.value, GExpr.Group.GroupType.group))
				)
				.or(parseExprBlock(GExpr.Group.GroupType.block))
				.or(parseExprBlock(GExpr.Group.GroupType.group))
				.or(parseCustom())
				.or(parseName)
				.onErrorAddMessage("Expected an expression primitive")
				.skip(ws)
				.onErrorAddMessage("Expected an expression factor")
				/*.and(parseApply().withPos()
								 .optional())
				.map(t ->
					t._2.<GExpr>map(args -> new GExpr.Apply(args.pos, t._1, args.value))
						.orElse(t._1)
				)*/
				.parse(source);

	public Parser<ETypeSig> parseType() {
		return source ->
			term(":").skipAnd(typeSigParser).optional().map(opt -> opt.orElse(ETypeSig.any))
					 .parse(source);
	}

	public Parser<GExpr.TypedName> parseTypedName =
		parseNameString()
			.and(parseType())
			.map(t -> new GExpr.TypedName(t._1.pos, t._1.value, t._2));
/*
	private Parser<Tuple2<WithPos<String>,ETypeSig>> parseVarDecl =
		parseNameString()
		.and(parseType());
*/

	public Parser<GExpr> parseLambda() {
		return source ->
			term("(").skipAnd(
				Parser.zeroOrMoreSep(
					parseTypedName,
					term(",")
				).skip(term(")"))
			)
					 .or(parseTypedName.map(arg -> PList.val(arg)))
					 .skip(term("->")).and(parseExpr())
					 .and(parseType())
				.<GExpr>map(t -> new GExpr.Lambda(source.position, t._2, t._1._1, t._1._2))
				.parse(source);

	}


	private GExpr createApply(GExpr left, StrPos namePos, String name, GExpr right) {
		GExpr.Child function = new GExpr.Child(left.getPos(), left, new GExpr.TypedName(namePos, name, ETypeSig.any));
		return new GExpr.Apply(left.getPos(), function, PList.val(right));
	}


	private Parser<GExpr> parseArrayAccess(GExpr left) {
		return
			term("[").skipAnd(parseExpr()).skip(term("]"))
					 .map(arr -> createApply(left, left.getPos(), "[]", arr));


	}

	private Parser<GExpr> parseChild(GExpr left) {
		return
			term(".").skipAnd(parseTypedName)
					 .map(typedName -> new GExpr.Child(typedName.pos, left, typedName));
	}

	private Parser<GExpr> parseApply(GExpr left) {
		return term("(")
			.skipAnd(Parser.zeroOrMoreSep(parseExpr(), term(",")).withPos())
			.skip(ws)
			.skip(term(")"))
			.map(argList -> new GExpr.Apply(argList.pos, left, argList.value));
	}

	private Parser<GExpr> parsePostL1() {
		return source -> {

			ParseResult<GExpr> left = parsePrimitiveL0.parse(source);
			if(left.isFailure()) {
				return left;
			}


			while(true) {
				GExpr leftExpr = left.getValue();
				ParseResult<GExpr> newLeft =
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

	private Parser<GExpr> parsePreUnaryL2 = parsePostL1();

		/*orTerms("++_","--_","+_","-_","!_","~_")
			.withPos()
				.and(parsePostL1())
				.map(t -> createApply(t._2,t._1.value,new GExpr.Const(t._1.pos,null)))
				.onErrorAddMessage("Expected an prefix unary operator.")
				.or(parsePostL1())
		;
		/*orTerms("++_","--_","+_","-_","!_","~_").withPos()
		.and(parsePostL1())
		.map(t -> createApply(t._2,t._1.value,new GExpr.Const(t._1.pos,null)))
		.onErrorAddMessage("Expected an prefix unary operator.")
		.or(parsePostL1())
		;*/

	private Parser<GExpr> parseCastAndNewL3 = parsePreUnaryL2;

	private Parser<GExpr> parseBinOpMultiplicativeL4 =
		parseBinOp(parseCastAndNewL3, orTerms("*", "/", "%"), parseCastAndNewL3)
			.skip(ws)
			.onErrorAddMessage("Expected a multiplicative operator.");

	private Parser<GExpr> parseBinOpAdditveL5 =
		parseBinOp(parseBinOpMultiplicativeL4, orTerms("+", "-"), parseBinOpMultiplicativeL4)
			.skip(ws)
			.onErrorAddMessage("Expected an additive operator.");


	private Parser<GExpr> parseBinOpShiftL6 =
		parseBinOp(parseBinOpAdditveL5, orTerms(">>>", "<<", ">>"), parseBinOpAdditveL5)
			.skip(ws)
			.onErrorAddMessage("Expected a shift operator ");

	private Parser<GExpr> parseBinOpComparisonL7 =
		parseBinOp(parseBinOpShiftL6, orTerms("<=", ">=", "<", ">"), parseBinOpShiftL6)
			.skip(ws)
			.onErrorAddMessage("Expected a comparison operator.");

	private Parser<GExpr> parseBinOpEqualityL8 =
		parseBinOp(parseBinOpComparisonL7, orTerms("==", "!="), parseBinOpComparisonL7)
			.skip(ws)
			.onErrorAddMessage("Expected an equality operator.");

	private Parser<GExpr> parseBinOpBitwiseANDL9 =
		parseBinOp(parseBinOpEqualityL8, term("&").and(Parser.not("", term("&"))).map(t -> t._1), parseBinOpEqualityL8)
			.skip(ws)
			.onErrorAddMessage("Expected a bitwise and operator.");

	private Parser<GExpr> parseBinOpBitwiseXORL10 =
		parseBinOp(parseBinOpBitwiseANDL9, term("^"), parseBinOpBitwiseANDL9)
			.skip(ws)
			.onErrorAddMessage("Expected a bitwise and operator.");

	private Parser<GExpr> parseBinOpBitwiseORL11 =
		parseBinOp(parseBinOpBitwiseXORL10, term("|").and(Parser.not("", term("|")))
													 .map(t -> t._1), parseBinOpBitwiseXORL10)
			.skip(ws)
			.onErrorAddMessage("Expected a bitwise and operator.");


	private Parser<GExpr> parseBinOpCondANDL12 =
		parseBinOp(parseBinOpBitwiseORL11, term("&&"), parseBinOpBitwiseORL11)
			.skip(ws)
			.onErrorAddMessage("Expected a bitwise and operator.");


	private Parser<GExpr> cast(Parser<GExpr> exprParser, Class cls) {
		return exprParser.map(g -> g.getType().equals(ETypeSig.cls(cls)) ? g : GExpr.cast(g, cls));
	}

	private Parser<GExpr> parseBinOpCondORL13 =
		parseBinOp(parseBinOpCondANDL12, term("||"), parseBinOpCondANDL12)
			.skip(ws)
			.onErrorAddMessage("Expected a bitwise and operator.");


	private Parser<GExpr> parseSimpleExpr = parseBinOpCondORL13;

	private Parser<GExpr> parseAssign(GExpr left) {
		return source -> term("=").skipAnd(parseExpr())
			.<GExpr>map(e -> new GExpr.BinOp(left, "=", e))
			.parse(source)
			;
	}

	private Parser<GExpr> parseVarVal() {
		return source ->
			keyword("val").map(v -> GExpr.ValVar.ValVarType.val)
						  .or(keyword("var").map(v -> GExpr.ValVar.ValVarType.var))
						  .and(parseTypedName)
						  .skip(term("="))
						  .and(parseExpr())
						  //.<GExpr>map(t -> new GExpr.ValVar(t._2.pos,t._2, t._1))
						  //.parseThisOrFollowedBy(vv -> parseAssign(vv))
				.<GExpr>map(t ->
					new GExpr.ValVar(t._1._2.pos, t._1._2, t._1._1, t._2)
				)
				.parse(source)
			;

	}

	private Parser<GExpr> parseVarValAndAssign() {
		return parseVarVal();
	}

	private Parser<PList<String>> nameList =
		term("(")
			.skipAnd(Parser.zeroOrMoreSep(Scan.identifier.skip(ws), term(",")))
			.skip(term(")"))
			.onErrorAddMessage("Expected name list");


	public Parser<GExpr> parseExprBlock(GExpr.Group.GroupType blockType) {
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
					  .map(t -> new GExpr.Group(t._2, blockType));
	}

	public Parser<GExpr> parseExpr() {
		return source ->
			parseVarValAndAssign()
				.or(parseSimpleExpr.parseThisOrFollowedBy(l -> parseAssign(l)))
				.skip(ws).parse(source);
	}

	public final Parser<String> parseEndOfExpression =
		term(";");

	public Parser<GExpr> parseExprList() {
		return source -> {
			StrPos       startPos = source.position;
			PList<GExpr> result   = PList.empty();
			while(true) {
				ParseResult<GExpr> exprResult = parseExpr().parse(source);
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
				new GExpr.ExprList(startPos, result.toImmutableArray())
			);
		};
	}


	public Parser<GExpr> parseBinOp(Parser<GExpr> left, Parser<String> op, Parser<GExpr> right) {
		return source -> {
			ParseResult<GExpr> leftRes = left.parse(source);
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
				ParseResult<GExpr> rightRes = right.parse(opRes.getSource());
				if(rightRes.isFailure()) {
					return rightRes;
				}
				GExpr result = new GExpr.BinOp(leftRes.getValue(), opRes.getValue(), rightRes.getValue());
				/*
					GExpr.Child function = new GExpr.Child(left.getPos(), left, name);
					return new GExpr.Apply(left.getPos(), function, PList.val(right));
				GExpr result =
					createApply(leftRes.getValue(), opRes.getSource().position, opRes.getValue(), rightRes.getValue());
				*/
				leftRes = ParseResult.success(rightRes.getSource(), result);
			}

		};
	}

	public Parser<WithPos<String>> parseNameString() {
		return source ->
			Scan.identifier
				.withPos()
				.skip(ws)
				.onErrorAddMessage("Expected a variable name")
				.parse(source)
			;
	}


	public final Parser<GExpr> parseStringLiteral =
		Scan.stringLiteral("\"\"\"", true)
			.or(Scan.stringLiteral("\'", false))
			.or(Scan.stringLiteral("`", false))
			.or(Scan.stringLiteral("\"", false))
			.skip(ws)
			.withPos()
			.map(value -> new GExpr.Const(value.pos, ETypeSig.cls(String.class), value.value));

	public final Parser<GExpr> parseNumberLiteral =
		Scan.doubleLiteral.withPos().<GExpr>map(v -> new GExpr.Const(v.pos, ETypeSig.cls(Double.class), v.value))
			.or(
				Scan.longLiteral
					.withPos()
					.and(
						Scan.term("L")
							.or(Scan.term("l"))
							.or(Scan.term("i"))
							.or(Scan.term("I"))
							.optional())
					.<GExpr>map(v -> {
						long    value = v._1.value;
						Integer asInt = UNumber.convertToInteger(value).orElse(null);
						if(v._2.isPresent()) {
							switch(v._2.get()) {
								case "L":
								case "l":
									return new GExpr.Const(v._1.pos, ETypeSig.cls(Long.class), value);
								default:
									if(asInt == null) {
										throw new ParseException("Not a valid integer literal: " + value, v._1.pos);
									}
									return new GExpr.Const(v._1.pos, ETypeSig.cls(Integer.class), asInt);
							}
						}
						if(asInt == null) {
							return new GExpr.Const(v._1.pos, ETypeSig.cls(Long.class), value);
						}
						return new GExpr.Const(v._1.pos, ETypeSig.cls(Integer.class), asInt);
					}))
			.skip(ws);

	public final Parser<GExpr> parseBooleanLiteral =
		keyword("true").withPos().<GExpr>map(s -> new GExpr.Const(s.pos, ETypeSig.cls(Boolean.class), true))
			.or(keyword("false").withPos().<GExpr>map(s -> new GExpr.Const(s.pos, ETypeSig.cls(Boolean.class), false)))
			.onErrorAddMessage("Expected 'true' or 'false'");

	public Parser<GExpr> parseConst() {
		return parseNumberLiteral
			.or(parseBooleanLiteral)
			.or(parseStringLiteral)
			.or(keyword("null").withPos().map(v -> new GExpr.Const(v.pos, ETypeSig.any, null)))
			.onErrorAddMessage("Expected a literal!");
	}

}
