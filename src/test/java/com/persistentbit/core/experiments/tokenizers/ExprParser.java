package com.persistentbit.core.experiments.tokenizers;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.Iterator;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/02/2017
 */
public class ExprParser extends Parser<ExprToken>{

    public interface LExpr{

    }
    public static class LName extends BaseValueClass implements LExpr{
        public final String name;

        public LName(String name) {
            this.name = name;
        }
    }
    public static class LLambda extends BaseValueClass implements LExpr{
        public final LExpr nameExpr;
        public final LExpr functionExpr;

        public LLambda(LExpr nameExpr, LExpr functionExpr) {
            this.nameExpr = nameExpr;
            this.functionExpr = functionExpr;
        }
    }
    public static class LApplication extends BaseValueClass implements LExpr{
        public final LExpr function;
        public final LExpr value;

        public LApplication(LExpr function, LExpr value) {
            this.function = function;
            this.value = value;
        }
    }


    public ExprParser(Iterator<Token<ExprToken>> tokensIterator) {
        super(tokensIterator);
    }


    static public Result<LExpr> parse(String name, String code){
        return new ExprParser(ExprTokenizer.tokenize(name,code)).parseExpr();
    }

    public Result<LName>    parseName(){
        return Result.function().code(l -> {
            if(current != ExprToken.tName){
                return error("Expected a name!");
            }
            return Result.success(new LName(textAndNext()));
        });
    }
    public Result<LLambda> parseLambda() {
        return Result.function().code(l -> {
            if(current != ExprToken.tLambda){
                return error("Expected a lambda symbol or '\\'!");
            }
            next();//skip lambda
            return parseName()
                .flatMap(name ->{
                    if(current != ExprToken.tPoint){
                        return error("Expected '.'");
                    }
                    next();//skip point
                    return parseExpr()
                            .map(expr -> new LLambda(name,expr));
                });

        });
    }
    public Result<LExpr> parseExpr() {
        return Result.function().code(l -> {
           switch (current){
               case tLambda: return parseLambda().map(v -> v);
               case tName: return parseName().map(v -> v);
               case eof: return Result.empty(new ParserException(currentToken.pos,"End of File"));
               default: return error("Unexpected token");
           }
        });
    }

    public static void main(String... args) throws Exception {
        LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();
        String code = "\\hello.hello";
        System.out.println(ExprParser.parse("test",code).orElseThrow());
    }
}
