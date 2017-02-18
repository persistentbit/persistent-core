package com.persistentbit.core.experiments.parser.lambda;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.experiments.parser.ParseResult;
import com.persistentbit.core.experiments.parser.ParseSource;
import com.persistentbit.core.experiments.parser.Parser;
import com.persistentbit.core.experiments.parser.Scan;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;

/**
 * TODOC
 *
 * @author petermuys
 * @since 18/02/17
 */
public class LambdaParser{

	static Parser<LambdaExpr> expr() {
		return source ->
			Parser.when("Not a valid Expression!",
						s -> Parser.or("?",lambdaStart,Scan.term("("),Scan.identifier).skipWhiteSpace().parse(s).isSuccess(),
						Parser.or("Not a valid Expression",
			   application,
			   lambda,
			   def,
			   var
		)).parse(source);
	}

	static Parser<String> defStart = Scan.term("def")
		.skipAndThen(Scan.whiteSpace);

	static Parser<LambdaExpr> def = source -> {
		return defStart
			.skipAndThen(Scan.identifier)
			.andThenSkip(Scan.whiteSpace)
			.andThenSkip(Scan.term("="))
			.andThenSkip(Scan.whiteSpace)
			.andThen(expr())
			.<LambdaExpr>map(t -> new LambdaExpr.Define(t._1,t._2))
			.parse(source);
	};

	static <T> Parser<T> group(Parser<T> parser){
		return Scan.term("(")
			.skipAndThen(parser)
			.andThenSkip(Scan.term(")"));
	}

	static Parser<LambdaExpr> applicationItem = source -> {
		ParseResult<LambdaExpr> funRes = expr().parse(source);
		if(funRes.isFailure()){
			return funRes;
		}
		ParseResult<PList<LambdaExpr>> restRes = Parser.oneOrMore("Expected application parameter",Scan.whiteSpace.skipAndThen(expr())).parse(source);
		if(restRes.isFailure()){
			return restRes.map(v -> null);
		}
		return ParseResult.success(restRes.getSource(), restRes.getValue().fold(funRes.getValue(),l->r-> new LambdaExpr.Apply(l,r)));

	};

	static Parser<LambdaExpr> application =
		group(applicationItem)
	;


	static Parser<String> lambdaStart =
		Parser.or("Expected '\\' or 'λ'", Scan.term("\\"), Scan.term("λ"));

	static Parser<LambdaExpr> lambda = source ->
		lambdaStart
			.skipAndThen(Scan.identifier)
			.andThenSkip(Scan.term("."))
			.andThen(expr())
			.<LambdaExpr>map(t -> new LambdaExpr.Lambda(t._1,t._2))
			.parse(source);
	;


	static Parser<LambdaExpr> var = Scan.identifier.map(name -> new LambdaExpr.Var(name));


	public static void main(String[] args) {
		LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();
		String src = "def test = bla";//(\\x.x \\a.\\b.b)";
		ParseResult<LambdaExpr> result = expr().andThenEof().parse(ParseSource.asSource("Lambda",src));
		System.out.println(result.getValue());
	}
}
