package com.persistbit.core.parser;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.parser.Scan;
import com.persistentbit.core.parser.WithPos;
import com.persistentbit.core.parser.source.Position;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;

import java.util.Optional;
import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 17/02/17
 */
public class ParserTest {

    interface Expr {
        <R> R match(
                Function<GroupExpr,R> groupExpr,
                Function<ConstExpr,R> constExpr,
                Function<VarExpr,R>   varExpr,
                Function<BinOpExpr, R> binOpExpr
        );
    }
    static abstract class BaseExpr implements Expr{
        private final Position pos;
        BaseExpr(Position pos){
            this.pos = pos;
        }

        public abstract <R> R match(
                Function<GroupExpr,R> groupExpr,
                Function<ConstExpr,R> constExpr,
                Function<VarExpr,R>   varExpr,
                Function<BinOpExpr, R> binOpExpr
        );

        public Optional<Position> getPos(){
            return Optional.ofNullable(pos);
        }
    }

    static class GroupExpr extends BaseExpr {

        private final Expr expr;

        public GroupExpr(Position pos, Expr expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public String toString() {
            return "(" + expr + ")";
        }

        @Override
        public <R> R match(Function<GroupExpr, R> groupExpr, Function<ConstExpr, R> constExpr, Function<VarExpr, R> varExpr, Function<BinOpExpr, R> binOpExpr) {
            return groupExpr.apply(this);
        }
    }

    static class ConstExpr extends BaseExpr {

        private final Object value;

        public ConstExpr(Position pos, Object value) {
            super(pos);
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        public <R> R match(Function<GroupExpr, R> groupExpr, Function<ConstExpr, R> constExpr, Function<VarExpr, R> varExpr, Function<BinOpExpr, R> binOpExpr) {
            return constExpr.apply(this);
        }
    }

    static class VarExpr extends BaseExpr {

        private final String varName;

        public VarExpr(Position pos, String varName) {
            super(pos);
            this.varName = varName;
        }

        @Override
        public String toString() {
            return varName;
        }

        @Override
        public <R> R match(Function<GroupExpr, R> groupExpr, Function<ConstExpr, R> constExpr, Function<VarExpr, R> varExpr, Function<BinOpExpr, R> binOpExpr) {
            return varExpr.apply(this);
        }
    }

    static class BinOpExpr extends BaseExpr {

        private final Expr left;
        private final String binOp;
        private final Expr right;

        public BinOpExpr(Position pos, Expr left, String binOp, Expr right) {
            super(pos);
            this.left = left;
            this.binOp = binOp;
            this.right = right;
        }

        @Override
        public String toString() {
            return left + " " + binOp + " " + right;
        }

        @Override
        public <R> R match(Function<GroupExpr, R> groupExpr, Function<ConstExpr, R> constExpr, Function<VarExpr, R> varExpr, Function<BinOpExpr, R> binOpExpr) {
            return binOpExpr.apply(this);
        }
    }

    public static Parser<String> ws = Scan.whiteSpace;
    public static Parser<String> term(String term) {
        return Scan.term(term).skip(ws);
    }

    public static Parser<Expr> parseFactorExpr = source -> Log.function().code(l -> {
        return
                Parser.or(
                        term("(")
                                .skipAndThen(parseExpr())
                                .andThenSkip(term(")"))
                                .withPos()
                                .map(e -> (Expr) new GroupExpr(e.pos,e.value)),
                        parseVar(),
                        parseConst()
                ).onErrorAddMessage("Expected a Variable or a literal or (<expr>)!")
                        .skip(ws)
                        .onErrorAddMessage("Expected an expression factor")
                        .parse(source);
    });

    public static Parser<Expr> parseTermExpr = source -> {
        return parseBinOp(
                parseFactorExpr,
                Parser.or(
                        term("*"), term("/"), term("and")
                ).onErrorAddMessage("Expected an expression term operator").skip(ws),
                parseFactorExpr
        )
                .skip(ws)
                .onErrorAddMessage("Expected an expression term")
                .parse(source);
    };
    public static Parser<Expr> parseSimpleExpr = source -> {
        Parser<Expr> parser = parseBinOp(
                parseTermExpr,
                Parser.or(term("+"), term("-"), term("or"))
                        .onErrorAddMessage("Expectedd a term operator")
                        .skip(ws),
                parseTermExpr
        ).skip(ws);
        return parser.parse(source);
    };

    public static Parser<Expr> parseExpr() {
        return parseSimpleExpr;
    }


    public static Parser<Expr> parseBinOp(Parser<Expr> left, Parser<String> op, Parser<Expr> right) {
        return source -> {
            ParseResult<WithPos<Expr>> leftRes = left.withPos().parse(source);
            if (leftRes.isFailure()) {
                return leftRes.map(v -> v.value);
            }
            while (true) {
                ParseResult<Optional<String>> opRes = op.optional().parse(leftRes.getSource());
                if (opRes.isFailure()) {
                    return opRes.map(v -> null);
                }
                String opResValue = opRes.getValue().orElse(null);
                if (opResValue == null) {
                    return leftRes.map(v -> v.value);
                }
                ParseResult<Expr> rightRes = right.parse(opRes.getSource());
                if (rightRes.isFailure()) {
                    return rightRes;
                }
                Position leftPos = leftRes.getValue().pos;
                leftRes = ParseResult.success(
                        rightRes.getSource(),
                        new WithPos(
                                leftPos,
                                new BinOpExpr(leftPos, leftRes.getValue().value, opResValue, rightRes.getValue()))
                );
            }
        };
    }


    public static Parser<Expr> parseVar() {
        return Scan.identifier.withPos().map(name -> new VarExpr(name.pos,name.value));
    }

    public static Parser<Expr> parseConst() {
        return Scan.integerLiteral.withPos().map(value -> new ConstExpr(value.pos,value.value));
    }


    static final TestCase simpleExpr = TestCase.name("parse simple expression").code(tr -> {
        String source = "(  1 + 2  ) * varName / 3 - 4 + ( 1234 / 1 * 0 ) - name";
        tr.info("org: " + source);
        Expr expr = parseExpr().andThenEof().parse(Source.asSource("test", source)).getValue();
        tr.info(expr);
        tr.info(new Printer().print(expr).printToString());
    });

    static class Printer{

        public PrintableText print(Expr expr){
            return expr.match(
                gr -> group(gr),
                ce -> constExpr(ce),
                var -> varExpr(var),
                binOp -> binOpExpr(binOp)
            );
        }
        private PrintableText group(GroupExpr v){
            return out ->{
              out.println("group: " + v.getPos().get());
              out.indent(in -> {
                  in.print(print(v.expr));
              });
            };
        }
        private PrintableText constExpr(ConstExpr v){
            return out-> {
              out.println("const: " + v.getPos().get());
              out.indent(in -> in.print(v.getValue()));
            };
        }
        private PrintableText varExpr(VarExpr v){
            return out-> {
                out.println("const: " + v.getPos().get());
                out.indent(in -> in.print(v.toString()));
            };
        }
        private PrintableText binOpExpr(BinOpExpr v){
            return out ->{
                out.println("Op " + v + ": " + v.getPos().get());
                out.indent(in -> {
                    in.println(print(v.left));
                    in.println(print(v.right));
                });
            };
        }

    }

    public void testAll() {
        TestRunner.runAndPrint(LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)), ParserTest.class);
    }

    public static void main(String[] args) {
        LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();
        new ParserTest().testAll();
    }
}
