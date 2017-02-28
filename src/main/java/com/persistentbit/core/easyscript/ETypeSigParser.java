package com.persistentbit.core.easyscript;

import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.parser.Scan;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 28/02/2017
 */
public class ETypeSigParser {

    private final Parser<String> whitespace;

    public ETypeSigParser(Parser<String> whitespace) {
        this.whitespace = whitespace;
    }

    private Parser<String> term(String value){
        return Scan.term(value).skip(whitespace);
    }
    public Parser<ETypeSig> parseGenericBound(ETypeSig left) {
        return
                term("extends").map(s -> ETypeSig.Bound.Type.boundExtends)
                .or(term("super").map(s -> ETypeSig.Bound.Type.boundSuper))
                .and(Parser.oneOrMoreSep(parseTypeSig(),term("&")))
                .map(t -> new ETypeSig.Bound(t._1, left, t._2) )
        ;
    }
    public Parser<ETypeSig> parseArray(ETypeSig left){
        return term("[")
                .skipAnd(term("]"))
                .map(v -> new ETypeSig.Array(left));
    }

    public Parser<ETypeSig> parseGeneric(ETypeSig left){
        return
            term("<")
                .skipAnd(
                    Parser.oneOrMoreSep(parseTypeSig(),term(","))
                ).skip(term(">"))
                .map(l -> new ETypeSig.WithGenerics(left,l));
    }

    public Parser<ETypeSig> parseName() {
        return Parser.oneOrMoreSep(Scan.identifier.skip(whitespace),term("."))
                .map(t -> new ETypeSig.Name(t.toString(".")))
                ;
    }
    public Parser<ETypeSig> parseAny() {
        return term("Any").map(s -> new ETypeSig.Any());
    }
    // List<T>[][]
    // <A>(A,A)->A

    public Parser<ETypeSig> parseTypeSig() {
        return
            Parser.orOf(
                term("Any").map(s -> new ETypeSig.Any())
        );
    }



}
