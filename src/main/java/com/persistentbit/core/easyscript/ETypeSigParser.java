package com.persistentbit.core.easyscript;

import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.parser.Scan;
import com.persistentbit.core.parser.source.Source;

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


	public ParseResult<ETypeSig> parse(Source source) {
		return parseNameWithGenerics().parse(source);
	}

	public ParseResult<ETypeSig> parse(String code) {
		return parseTypeSig().parse(Source.asSource(code));
	}


    private Parser<String> term(String value){
        return Scan.term(value).skip(whitespace);
    }


	private Parser<ETypeSig> parseAny() {
		return term("Any").or(term("?")).map(s -> new ETypeSig.Any());
	}


	private Parser<ETypeSig> parseGenericBound(ETypeSig left) {
		return
			term("extends").map(s -> ETypeSig.Bound.Type.boundExtends)
						   .or(term("super").map(s -> ETypeSig.Bound.Type.boundSuper))
						   .and(Parser.oneOrMoreSep(parseTypeSig(), term("&")))
						   .map(t -> new ETypeSig.Bound(t._1, left, t._2))
			;
	}

	private Parser<ETypeSig> parseAnyWithBound() {
		return
			parseAny().parseThisOrFollowedBy(this::parseGenericBound);
	}


	private Parser<ETypeSig> parseName() {
		return Parser.oneOrMoreSep(Scan.identifier.skip(whitespace), term("."))
					 .map(t -> new ETypeSig.Name(t.toString(".")))
			;
	}

	private Parser<ETypeSig> parseGeneric(ETypeSig left) {
		return
			term("<")
				.skipAnd(
					Parser.oneOrMoreSep(
						parseAnyWithBound().or(parseTypeSig())
						, term(",")
					)
				).skip(term(">"))
				.map(l -> new ETypeSig.WithGenerics((ETypeSig.Name) left, l));
	}

	private Parser<ETypeSig> parseNameWithGenerics() {
		return parseName().parseThisOrFollowedBy(this::parseGeneric);

	}

	private Parser<ETypeSig> parseFunction() {
		return
			term("(")
				.skipAnd(Parser.zeroOrMoreSep(parseTypeSig(), term(",")))
				.skip(term(")"))
				.skip(term("->"))
				.and(parseTypeSig())
				.map(t -> new ETypeSig.Fun(t._2, t._1));
	}

	private Parser<ETypeSig> parseSimple() {
		return Parser.orOf(parseAny(), parseNameWithGenerics(), parseFunction());
	}


	private Parser<ETypeSig> parseArray(ETypeSig left) {
		return term("[")
			.skipAnd(term("]"))
			.map(v -> new ETypeSig.Array(left));
	}

	private Parser<ETypeSig> parseSimpleWithArray() {
		return parseSimple().parseThisOrFollowedBy(
			left -> parseArray(left).parseThisOrFollowedBy(arleft -> parseArray(arleft))
		);
	}

	private Parser<ETypeSig> parseTypeSig() {
		return source ->
			parseSimpleWithArray().or(whitespace.map(s -> new ETypeSig.Any())).parse(source))
	}
	}



}
