package com.persistentbit.core.experiments.parser;

import com.persistentbit.core.OK;
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
	public static Parser<String> whiteSpace = source -> {
		String res = "";
		while(Character.isWhitespace((char) source.current())) {
			res = res + ((char) source.current());
			source = source.next();
		}
		return ParseResult.success(source, res);
	};

	/**
	 * Scan the EOF character.
	 *
	 * @return Success when the current char was an EOF.
	 */
	public static Parser<OK> eof = source -> {
		if(source.current() == ParseSource.EOF) {
			return ParseResult.success(source, OK.inst);
		}
		return ParseResult.failure(source, "Expected end-of-file!");

	};

	/**
	 * Parse a Unicode Java Identifier
	 */
	public static Parser<String> identifier = source -> {
		if(Character.isUnicodeIdentifierStart((char) source.current()) == false) {
			return ParseResult.failure(source,"Not a valid identifier");
		}
		String res = "" + (char) source.current();
		source = source.next();
		while(Character.isUnicodeIdentifierPart((char) source.current())) {
			res = res + (char) source.current();
			source = source.next();
		}
		return ParseResult.success(source, res);
	};

	public static Parser<Integer> integerLiteral = source -> {
		String result = "";
		while(Character.isDigit(source.current())) {
			result = result + (char) source.current();
			source = source.next();
		}
		if(result.isEmpty()) {
			return ParseResult.failure(source, "Expected an integer literal");
		}
		return ParseResult.success(source, NumberUtils.parseInt(result).orElseThrow());
	};
	public static Parser<String> term(String terminal){
		return source -> {
			for(int t=0; t< terminal.length(); t++){
				char c = terminal.charAt(t);
				int sc = source.current();
				if(c != sc){
					return ParseResult.failure(source,"Expected '" + terminal + "'");
				}
				source = source.next();
			}
			return ParseResult.success(source, terminal);
		};

	}
}
