package com.persistentbit.core.experiments.parser.lambda;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.parser.Scan;
import com.persistentbit.core.parser.source.Source;

/**
 * TODOC
 *
 * @author petermuys
 * @since 18/02/17
 */
public class LambdaParser {

    static Parser<LambdaExpr> expr() {
        return source ->
                Parser.or(
                        application,
                        lambda,
                        def,
                        var
                ).parse(source);
    }

    static Parser<String> defStart =
            Scan.term("def")
                    .skipAndThen(Scan.whiteSpace)
                    .onErrorAddMessage("Excpected 'def'");

    static Parser<LambdaExpr> def = source ->
            defStart
                    .skipAndThen(Scan.identifier)
                    .andThenSkip(Scan.whiteSpace)
                    .andThenSkip(Scan.term("="))
                    .andThenSkip(Scan.whiteSpace)
                    .andThen(expr())
                    .<LambdaExpr>map(t -> new LambdaExpr.Define(t._1, t._2))
                    .onErrorAddMessage("Expected a definition!")
                    .parse(source);

    static <T> Parser<T> group(Parser<T> parser) {
        return Scan.term("(")

                .skipAndThen(parser)
                .andThenSkip(Scan.term(")"))
                .onErrorAddMessage("Expected a group")
                ;

    }

    static Parser<LambdaExpr> applicationItem = source -> {
        ParseResult<LambdaExpr> funRes = expr().onErrorAddMessage("Expected application function").parse(source);
        if (funRes.isFailure()) {
            return funRes;
        }
        source = funRes.getSource();
        ParseResult<PList<LambdaExpr>> restRes =
                Parser.oneOrMore("Expected application parameter", Scan.whiteSpace.skipAndThen(expr())).parse(source);
        if (restRes.isFailure()) {
            return restRes.map(v -> null);
        }
        /*ParseResult<PList<LambdaExpr>> restRes =
            Scan.whiteSpace
				.skipAndThen(expr())
				.map(v -> PList.val(v))
				.parse(funRes.getSource());
		*/
        LambdaExpr res = restRes.getValue().fold(funRes.getValue(),l -> r -> (LambdaExpr)new LambdaExpr.Apply(l, r));
        return ParseResult.success(restRes.getSource(),res);
    };

    static Parser<LambdaExpr> application =
            group(applicationItem).onErrorAddMessage("Expected an application");


    static Parser<String> lambdaStart =
            Parser.or(Scan.term("\\"), Scan.term("λ")).onErrorAddMessage("Expected '\\' or 'λ'")
                    .onErrorAddMessage("Expected a Lambda start symbol");

    static Parser<LambdaExpr> lambda = source ->
            lambdaStart
                    .skipAndThen(Scan.identifier)
                    .andThenSkip(Scan.term("."))
                    .andThen(expr())
                    .<LambdaExpr>map(t -> new LambdaExpr.Lambda(t._1, t._2))
                    .onErrorAddMessage("Expected a lambda exception")
                    .parse(source);


    static Parser<LambdaExpr> var =
            Parser.not("Unexpected keyword", Scan.term("def"))
                    .skipAndThen(Scan.identifier)
                    .<LambdaExpr>map(name -> new LambdaExpr.Var(name));


    public static void main(String[] args) {
        LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();
        String src = "def test = (\\x.x \\a.\\b.b (c d))";
        ParseResult<LambdaExpr> result = expr().parse(Source.asSource("Lambda", src));
        System.out.println(result.getValue().getClass().getSimpleName() + ": " + result.getValue());
    }
}
