package com.persistentbit.core.experiments.parser;

import com.persistentbit.core.utils.NumberUtils;

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
	}

	static class ConstExpr implements Expr{
		private final Object value;

		public ConstExpr(Object value) {
			this.value = value;
		}
	}

	static class VarExpr implements Expr{
		private String varName;

		public VarExpr(String varName) {
			this.varName = varName;
		}
	}

	static class BinOpExpr implements Expr{
		private final Expr left;
		private final String binOp;
		private final Expr right;

		public BinOpExpr(Expr left, String binOp, Expr right) {
			this.left = left;
			this.binOp = binOp;
			this.right = right;
		}
	}



	public static Parser<String> parseTerminal(String keyword){
		return source -> {
			for(int t=0; t< keyword.length(); t++){
				char c = keyword.charAt(t);
				int sc = source.current();
				if(c != sc){
					return ParseResult.failure(source,"Expected '" + keyword + "'");
				}
				source = source.next();
			}
			return ParseResult.success(source, keyword);
		};
	}

	public static Parser<Integer> parseIntegerLiteral(){
		return source -> {
			String result = "";
			while(Character.isDigit(source.current())){
				result = result + source.current();
				source = source.next();
			}
			if(result.isEmpty()){
				return ParseResult.failure(source,"Expected an integer literal");
			}
			return ParseResult.success(source,NumberUtils.parseInt(result).orElseThrow());
		};
	}

	public static Parser<Expr> parseExpr = parseSimpleExpr();



	public static Parser<Expr> parseBinOp(Parser<Expr> left, Parser<String> op, Parser<Expr> right){
		return left.andThen(
			op.andThen(right).optional()
		).map(t -> {
			if(t._2.isPresent() == false){
				return t._1;
			}
			return new BinOpExpr(t._1,t._2.get()._1,t._2.get()._2);
		});
	}

	public static Parser<Expr> parseSimpleExpr(){

		return parseBinOp(
			parseTermExpr(),
			parseTerminal("+")
				.or(parseTerminal("-"))
				.or(parseTerminal("or"))
				.skipWhiteSpace(),
			parseTermExpr()
		).skipWhiteSpace();
	}

	public static Parser<Expr> parseTermExpr() {
		return parseBinOp(
			parseFactorExpr(),
			parseTerminal("*")
				.or(parseTerminal("/"))
				.or(parseTerminal("and")),
			parseFactorExpr()
		).skipWhiteSpace();
	}

	public static Parser<Expr> parseFactorExpr() {
		return
			parseTerminal("(")
				.skipAndThen(parseExpr)
				.andThenSkip(parseTerminal(")"))
				.map(e -> (Expr)new GroupExpr(e))
			.or(parseVar())
			.or(parseConst())
			.skipWhiteSpace();
	}

	public static Parser<Expr> parseVar(){
		return parseIdentifier().map(name -> new VarExpr(name));
	}

	public static Parser<Expr> parseConst(){
		return parseIntegerLiteral().map(value -> new ConstExpr(value));
	}

	public static Parser<String> parseIdentifier(){
		return source -> ParseResult.failure(source,"Not implemented");
	}

	public static void main(String[] args) {
		String source="1+2*3";
		parseExpr.parse(ParseSource.asSource("test",source));
	}
}
