package com.persistentbit.core.config;


import com.persistentbit.core.collections.PList;
import com.persistentbit.core.keyvalue.NamedValue;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.parser.Scan;

import java.math.BigDecimal;


/**
 * TODO: Add comment
 *
 * @author Peter Muys
 */
public class ValueParser {

    //static private final Parser<String> ws =
    //        Parser.zeroOrMore(
    //                Scan.whiteSpaceAndNewLine.or(Scan.blockComment("/*","*/"))
    //        ).map(l -> l.toString(""));
    static private final Parser<String> ws =Scan.whiteSpaceAndNewLine;
    static private final Parser<BigDecimal> parseNumber = Scan.bigDecimalLiteral;
    static private final Parser<String> parseString =
            Scan.stringLiteral("\"",false)
            .or(Scan.stringLiteral("\'", false))
            .or(Scan.stringLiteral("\"\"\"",true));
    static private final Parser<Boolean> parseBool =
            Scan.term("true").map(s -> Boolean.TRUE)
                    .or(Scan.term("false").map(s ->  Boolean.FALSE));
    static private final Parser<String> parseName =
            Parser.oneOrMoreSep(Scan.identifier,Scan.term(".")).map(l -> l.toString("."));
    static private final Parser<Object> parseValue =
            parseNumber.map(v -> (Object)v)
                    .or(parseString.map(v -> (Object)v))
                    .or(parseBool.map(v -> (Object)v));


    private static  Parser<PList<NamedValue<Object>>> parse(){
        return source -> {
            source = ws.parse(source).getSource();
            ParseResult<String> resName = parseName.parse(source);
            if(resName.isFailure()){
                return resName.onErrorAdd("Expected a property name").map(v-> null);
            }
            source = resName.getSource();
            ParseResult<String>  div = ws.skipAnd(Scan.term("=").or(Scan.term("{"))).skip(ws).parse(source);
            if(div.isFailure()){
                return div.onErrorAdd("Expected { or = after " + resName.getValue()).map(v-> null);
            }
            source  = div.getSource();
            if(div.getValue().equals("=")){
                ParseResult<Object> resValue = ws.skipAnd(parseValue).parse(source);
                if(resValue.isFailure()){
                    return resValue.onErrorAdd("Expected a value for property " + resName.getValue()).map(v-> null);
                }

                return ParseResult.success(resValue.getSource(),PList.val(new NamedValue<>(resName.getValue(),resValue.getValue())));
            }
            ParseResult<PList<NamedValue<Object>>> resGroup =
                    Parser.zeroOrMore(parse())
                            .skip(ws.and(Scan.term("}")))
                            .map(ll -> ll.<NamedValue<Object>>flatten().plist())
                            .parse(source);
            if(resGroup.isFailure()){
                return resGroup.map(v -> null);
            }
            return resGroup.map(l -> l.map(nv -> new NamedValue<>(nv.tuple().map1(n-> resName.getValue() + "." + n))));

        };
    }

    public static Parser<PList<NamedValue<Object>>> parseAll(){
        return Parser.zeroOrMore(parse().skip(ws)).andEof().map(ll -> ll.<NamedValue<Object>>flatten().plist());
    }
}
