package com.persistentbit.core.parser;

import com.persistentbit.core.OK;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.utils.NumberUtils;

import java.util.function.Predicate;

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
		while(source.current() != Source.EOF && Character.isWhitespace(source.current())) {
			res = res + (source.current());
			source = source.next();
		}
		return ParseResult.success(source, res);
	};



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
	public static Parser<String> identifier = source -> {
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
	};

	public static Parser<Integer> integerLiteral = source -> {
		String result = "";
		while(Character.isDigit(source.current())) {
			result = result + source.current();
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
	public static Parser<String> lineComment(String startLineComment){
		return term(startLineComment).andThen(restOfLine).map(t -> t._1+t._2);
	}
	public static Parser<String> blockComment(String startComment, String endComment) {
		return
				Scan.term(startComment)
						.andThen(parseWhile(s -> s.endsWith("*/") == false))
						.map(t -> t._1 + t._2);
	}

	public static Parser<String> parseWhile(Predicate<String> predicate) {
		return source -> {
			String res = "";
			while (predicate.test(res)) {
				if (source.current() == Source.EOF) {
					return ParseResult.failure(source, "Unexpected end-of-file");
				}
				res = res + source.current();
				source = source.next();
			}
			return ParseResult.success(source, res);
		};
	}

	public static Parser<String> parseWhiteSpaceWithComment(Parser<String> whitespace, Parser<String> comment) {
		return Parser.zeroOrMore(
				whitespace
						.andThen(comment.optional().map(opt -> opt.orElse("")))
						.andThen(whitespace)
						.map(t -> t._1._1 + t._1._2 + t._2)
		).map(l -> l.toString(""));

	}
	/**
	 * One of CR, LF, CR-LF
	 */
	static Parser<String> nl = source -> {
		char c = source.current();
		if(c != '\r' && c != '\n'){
			return ParseResult.failure(source,"CR or LF expected");
		}
		source = source.next();
		if(c == '\n'){
			return ParseResult.success(source,"\n");
		}
		char c2 = source.current();
		if(c2 == '\n'){
			return ParseResult.success(source, "\r\n");
		}
		return ParseResult.success(source,"\r");

	};
	/**
	 * The rest of the current line without the newLine
	 */
	static Parser<String> restOfLine = source -> {
		String res = "";
		while(source.current() != Source.EOF && source.current()!='\r' && source.current()!='\n'){
			res += source.current();
			source = source.next();
		}
		return ParseResult.success(source,res);
	};
}
