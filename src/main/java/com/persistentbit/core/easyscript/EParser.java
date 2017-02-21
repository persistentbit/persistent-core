package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.parser.Scan;
import com.persistentbit.core.parser.WithPos;
import com.persistentbit.core.utils.StrPos;

import java.util.Optional;

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

	public static Parser<EExpr> parseFactorExpr = source -> Log.function().code(l -> {
		return
			Parser.orOf(
				term("(")
					.skipAnd(parseExpr())
					.skip(term(")"))
					.withPos()
					.map(e -> new EExpr.Group(e.pos, e.value, EExpr.Group.Type.group)),
				parseName(),
				parseConst()
			).onErrorAddMessage("Expected a Variable orOf a literal orOf (<expr>)!")
				  .skip(ws)
				  .onErrorAddMessage("Expected an expression factor")
				  .and(parseApply().withPos().optional())
				  .map(t ->
					  t._2.<EExpr>map(args -> new EExpr.Apply(args.pos, t._1, args.value)).orElse(t._1)
				  )
				  .parse(source);
	});

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
	public static Parser<EExpr> parseSimpleExpr = source -> {
		Parser<EExpr> parser = parseBinOp(
			parseTermExpr,
			Parser.orOf(term("+"), term("-"), term("or"))
				  .onErrorAddMessage("Expectedd a term operator")
				  .skip(ws),
			parseTermExpr.skip(ws)
		).skip(ws);
		return parser.parse(source);
	};

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
			ParseResult<WithPos<EExpr>> leftRes = left.withPos().parse(source);
			if(leftRes.isFailure()) {
				return leftRes.map(v -> v.value);
			}
			while(true) {
				ParseResult<Optional<String>> opRes = op.optional().parse(leftRes.getSource());
				if(opRes.isFailure()) {
					return opRes.map(v -> null);
				}
				String opResValue = opRes.getValue().orElse(null);
				if(opResValue == null) {
					return leftRes.map(v -> v.value);
				}
				ParseResult<EExpr> rightRes = right.parse(opRes.getSource());
				if(rightRes.isFailure()) {
					return rightRes;
				}
				StrPos leftPos = leftRes.getValue().pos;
				leftRes = ParseResult.success(
					rightRes.getSource(),
					new WithPos(
						leftPos,
						new EExpr.BinOp(leftPos, leftRes.getValue().value, opResValue, rightRes.getValue())
					)
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
				.<EExpr>map(t ->
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


	public static Parser<EExpr> parseConst() {
		return parseNumberLiteral.or(parseStringLiteral).onErrorAddMessage("Expected a literal!");
	}


}
