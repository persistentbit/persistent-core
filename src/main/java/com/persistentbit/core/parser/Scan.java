package com.persistentbit.core.parser;

import com.persistentbit.core.OK;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.utils.UNumber;
import com.persistentbit.core.utils.UString;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODOC
 *
 * @author petermuys
 * @since 18/02/17
 */
public class Scan{

	/**
	 * Scan all the whitespace characters, excluding new-line's
	 * defined by {@link Character#isWhitespace(char)}
	 */
	public static Parser<String> whiteSpace = source -> {
		String res = "";

		while(source.current != Source.EOF
			&& source.current != '\n'
			&& source.current != '\r'
			&& Character.isWhitespace(source.current)) {
			res = res + (source.current);
			source = source.next();
		}
		return ParseResult.success(source, res);
	};
	/**
	 * Scan all the whitespace characters, including new-line's
	 * defined by {@link Character#isWhitespace(char)}
	 */
	public static Parser<String> whiteSpaceAndNewLine = source -> {
		String res = "";

		while(source.current != Source.EOF
			&& Character.isWhitespace(source.current)) {
			res = res + (source.current);
			source = source.next();
		}
		return ParseResult.success(source, res);
	};


	public static Parser<String> regEx(String regExpr) {
		Pattern p = Pattern.compile("\\A(" + regExpr + ")", Pattern.DOTALL | Pattern.MULTILINE);

		return source -> {
			Matcher m = p.matcher(source.rest());
			if(m.find()) {
				String txt = m.group();
				return ParseResult.success(source.next(txt.length()), txt);
			}
			return ParseResult.failure(source, "No match for regEx '" + p.pattern() + "'");
		};
	}


	/**
	 * Scan the EOF character.
	 *
	 */
	public static Parser<OK> eof = source -> {
		if(source.current == Source.EOF) {
			return ParseResult.success(source, OK.inst);
		}
		return ParseResult
			.failure(source, new ParseExceptionEOF(
				"Expected end-of-file: got '" + UString
					.escapeToJavaString("" + source.current) + "'", source.position
			));

	};

	/**
	 * Parse a Unicode Java Identifier
	 */
	public static Parser<String> identifier = source -> {
		if(source.current == Source.EOF || Character.isUnicodeIdentifierStart(source.current) == false) {
			return ParseResult.failure(source,"Not a valid identifier");
		}
		String res = "" + source.current;
		source = source.next();
		while(source.current != Source.EOF && Character.isUnicodeIdentifierPart(source.current)) {
			res = res + source.current;
			source = source.next();
		}
		return ParseResult.success(source, res);
	};

	public static Parser<Integer> integerLiteral =
		Scan.regEx("[+-]?[0-9]+")
			.mapResult(res -> {
				if(res.isFailure()) {
					return res.<Integer>map(v -> null).onErrorAdd("Not an integer literal");
				}
				return UNumber.parseInt(res.getValue()).match(
					success -> res.map(v -> success.getValue()),
					empty -> ParseResult.failure(res.getSource(), "No Number"),
					failure -> ParseResult.failure(res.getSource(), failure.getException().getMessage())
				);
			});
	public static Parser<Long> longLiteral =
		Scan.regEx("[+-]?[0-9]+")
			.mapResult(res -> {
				if(res.isFailure()) {
					return res.<Long>map(v -> null).onErrorAdd("Not a long literal");
				}
				return UNumber.parseLong(res.getValue()).match(
					success -> res.map(v -> success.getValue()),
					empty -> ParseResult.failure(res.getSource(), "No Number"),
					failure -> ParseResult.failure(res.getSource(), failure.getException().getMessage())
				);
			});

	public static Parser<Double> doubleLiteral =
		Scan.regEx("[+-]?[0-9]*[.][0-9]+")
			.mapResult(res -> {
				if(res.isFailure()) {
					return res.<Double>map(v -> null).onErrorAdd("Not a double literal");
				}
				return UNumber.parseDouble(res.getValue()).match(
					success -> res.map(v -> success.getValue()),
					empty -> ParseResult.failure(res.getSource(), "No Number"),
					failure -> ParseResult.failure(res.getSource(), failure.getException().getMessage())
				);
			});

	public static Parser<String> endsWith(String endString){
		return source -> {
			Source startPos = source;
			StringBuilder sb = new StringBuilder(10);
			while(source.isEOF() == false && source.rest().startsWith(endString) == false){
				sb = sb.append(source.current);
				source = source.next();
			}
			if(source.isEOF()){
				return ParseResult.failure(startPos,"Expected '" + endString + "'");
			}
			for(int t=0; t<endString.length();t++){
				source = source.next();
			}
			return ParseResult.success(source,sb.toString() + endString);
		};
	}

	/**
	 * Parse a Java String literal.<br>
	 * The string must start and end with the supplied stringDelimiter char.<br>
	 * String can contain escaped chars like in java/javascript: \t, \\, \b, \r, \n, \/
	 * and can contain unicode chars in the form of \\uXXXX where XXXX is a hexadecimal number.
	 *
	 * @param stringDelimter The literal start/end string
	 * @param multiLine      Can the literal span multiple lines?
	 *
	 * @return The Resulting string without delimiters.
	 */
	public static Parser<String> stringLiteral(String stringDelimter, boolean multiLine) {
		return source -> {
			if(source.rest().startsWith(stringDelimter) == false) {
				return ParseResult.failure(source, "Not a String literal.");
			}
			Source orgSource = source;
			source = source.next(stringDelimter.length());
			StringBuilder sb = new StringBuilder(10);

			while(true) {
				if(source.isEOF()) {
					return ParseResult.failure(orgSource, "String literal is not terminated!");
				}
				if(source.current == '\n' && multiLine == false) {
					return ParseResult.failure(orgSource, "String literal is not terminated on the same line!");
				}
				if(source.rest().startsWith(stringDelimter)) {
					source = source.next(stringDelimter.length());
					return ParseResult.success(source, sb.toString());
				}
				if(source.current == '\\') {
					source = source.next();
					switch(source.current) {
						case '\\':
							sb.append('\\');
							break;
						case '\"':
							sb.append('\"');
							break;
						case '\'':
							sb.append('\'');
							break;
						case 'b':
							sb.append('\b');
							break;
						case 'r':
							sb.append('\r');
							break;
						case 'n':
							sb.append('\n');
							break;
						case 't':
							sb.append('\t');
							break;
						case '/':
							sb.append('/');
							break;
						case 'u':
							source = source.next();
							String hn = Character.toString(source.current);
							source = source.next();
							hn += source.current;
							source = source.next();
							hn += source.current;
							source = source.next();
							hn += source.current;
							source = source.next();
							String uc = UNumber.parseHexInt(hn).map(i -> new String(Character.toChars(i)))
											   .orElse("\\u" + hn);
							sb.append(uc);

							break;
						default:
							sb.append('\\');
							sb.append(source.current);
							break;
							//return ParseResult.failure(source, "Invalid escape sequence!");
					}
				}
				else {
					sb.append(source.current);
					source = source.next();
				}
			}

		};
	}



	public static Parser<String> term(String terminal){
		return source -> {
			for(int t = 0; t< terminal.length(); t++) {
				char c = terminal.charAt(t);
				int sc = source.current;
				if(c != sc){
					return ParseResult.failure(source,"Expected '" + terminal + "'");
				}
				source = source.next();
			}
			return ParseResult.success(source, terminal);
		};

	}
	public static Parser<String> lineComment(String startLineComment) {
		return term(startLineComment).and(restOfLine).map(t -> t._1+t._2);
	}
	public static Parser<String> blockComment(String startComment, String endComment) {
		return
			Scan.term(startComment)
				.and(parseWhile(s -> s.endsWith("*/") == false))
						.map(t -> t._1 + t._2);
	}

	public static Parser<String> parseUntilChar(Predicate<Source> predicate){
		return source -> {
			String res = "";
			while(predicate.test(source)) {
				if (source.current == Source.EOF) {
					return ParseResult.failure(source, "Unexpected end-of-file");
				}
				res = res + source.current;
				source = source.next();
			}
			return ParseResult.success(source, res);
		};
	}


	public static Parser<String> parseWhile(Predicate<String> predicate) {
		return source -> {
			String res = "";
			while(predicate.test(res)) {
				if (source.current == Source.EOF) {
					return ParseResult.failure(source, "Unexpected end-of-file");
				}
				res = res + source.current;
				source = source.next();
			}
			return ParseResult.success(source, res);
		};
	}

	public static Parser<String> parseWhiteSpaceWithComment(Parser<String> whitespace, Parser<String> comment) {
		return
			whitespace
				.and(Parser.zeroOrMore(
					comment.and(whitespace).map(t -> t._1 + t._2))
				)
				.onErrorAddMessage("Expected whitespace")
					.map(t -> t._1 + t._2.toString(""));
	}
	/**
	 * One of CR, LF, CR-LF
	 */
	public static Parser<String> nl = source -> {
		char c = source.current;
		if(c != '\r' && c != '\n') {
			return ParseResult.failure(source,"CR orOf LF expected");
		}
		source = source.next();
		if(c == '\n') {
			return ParseResult.success(source,"\n");
		}
		char c2 = source.current;
		if(c2 == '\n'){
			return ParseResult.success(source, "\r\n");
		}
		return ParseResult.success(source,"\r");

	};
	/**
	 * The rest of the current line without the newLine
	 */
	public static Parser<String> restOfLine = source -> {
		String res = "";
		while(source.current != Source.EOF && source.current != '\r' && source.current!='\n'){
			res += source.current;
			source = source.next();
		}
		return ParseResult.success(source,res);
	};

	public static Parser<String> keyword(String keyword) {
		return source -> {
			String res = "";
			while(source.current != Source.EOF && Character.isUnicodeIdentifierPart(source.current)) {
				res = res + source.current;
				source = source.next();
			}
			if(res.equals(keyword)) {
				return ParseResult.success(source, res);
			}
			return ParseResult.failure(source, "Expected keyword '" + keyword + "'");
		};
	}
}
