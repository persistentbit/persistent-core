package com.persistentbit.core.parser;

import com.persistentbit.core.OK;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.utils.NumberUtils;

/**
 * TODOC
 *
 * @author petermuys
 * @since 18/02/17
 */
public class Scan{

	/**
	 * Scan all the whitespace characters,
	 * defined by {@link Character#isWhitespace(char)}
	 */
	public static Parser<String> whiteSpace = Parser.named("whitespace", source -> {
		String res = "";
		while(source.current() != Source.EOF && Character.isWhitespace(source.current())) {
			res = res + (source.current());
			source = source.next();
		}
		return ParseResult.success(source, res);
	});

	/**
	 * Scan the EOF character.
	 *
	 */
	public static Parser<OK> eof = source -> {
		if(source.current() == Source.EOF) {
			return ParseResult.success(source, OK.inst);
		}
		return ParseResult.failure(source, "Expected end-of-file: got '" + source.current() + "'");

	};

	/**
	 * Parse a Unicode Java Identifier
	 */
	public static Parser<String> identifier = Parser.named("identifier", source -> {
		if(source.current() == Source.EOF || Character.isUnicodeIdentifierStart(source.current()) == false) {
			return ParseResult.failure(source,"Not a valid identifier");
		}
		String res = "" + source.current();
		source = source.next();
		while(source.current() != Source.EOF && Character.isUnicodeIdentifierPart(source.current())) {
			res = res + source.current();
			source = source.next();
		}
		return ParseResult.success(source, res);
	});

	public static Parser<Integer> integerLiteral = Parser.named("integerLiteral", source -> {
		String result = "";
		while(Character.isDigit(source.current())) {
			result = result + source.current();
			source = source.next();
		}
		if(result.isEmpty()) {
			return ParseResult.failure(source, "Expected an integer literal");
		}
		return ParseResult.success(source, NumberUtils.parseInt(result).orElseThrow());
	});
	public static Parser<String> term(String terminal){
		return Parser.named("Term[" + terminal + "]", source -> {
			for(int t=0; t< terminal.length(); t++){
				char c = terminal.charAt(t);
				int sc = source.current();
				if(c != sc){
					return ParseResult.failure(source,"Expected '" + terminal + "'");
				}
				source = source.next();
			}
			return ParseResult.success(source, terminal);
		});

	}
}
