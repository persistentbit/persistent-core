package com.persistentbit.core.tokenizer;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.tuples.Tuple3;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Simple Tokenizer (Lexer) can transform your text into tokens.<br>
 * You add token parser by adding {@link TokenMatcher}s to the tokenizer.<br>
 * The way this works is that the tokenizer loops through the matchers one by one and if a token is found,
 * then this is added to the result {@link Token} list.<br>
 * This tokenizer is not build for speed, but for easy usage. If you want to have more speed, you can still build a lexer
 * by hand.<br>
 *
 * @author Peter Muys
 * @see Token
 * @see TokenMatcher
 * @see TokenFound
 */
public class SimpleTokenizer<TT>{


	private PList<TokenMatcher<TT>> tokenMatchers = PList.empty();

	/**
	 * Add a token matcher to the list of matchers
	 *
	 * @param tokenMatcher The matcher to add
	 *
	 * @return this.
	 */
	public SimpleTokenizer<TT> add(TokenMatcher<TT> tokenMatcher) {
		return Log.function(tokenMatcher).code(l -> {
			tokenMatchers = tokenMatchers.plus(Objects.requireNonNull(tokenMatcher));
			return this;
		});
	}

	/**
	 * A Token matcher that uses a Regular Expression to match the token.
	 *
	 * @param regex The regular Expression to match
	 * @param type  The resulting token type
	 * @param <TT>  The Type of the Token Type
	 *
	 * @return The TokenMatcher
	 */
	public static <TT> TokenMatcher<TT> regExMatcher(String regex, TT type) {
		return new TokenMatcher<TT>(){
			private final Pattern pattern = Pattern.compile("\\A(" + regex + ")", Pattern.DOTALL | Pattern.MULTILINE);

			@Override
			public Result<TokenFound<TT>> tryParse(String code) {
				return Result.function(code).code(l -> {
					if(code == null) {
						return Result.failure("code is null");
					}
					Matcher m = pattern.matcher(code);
					if(m.find()) {
						String txt = m.group();
						return Result.success(new TokenFound<>(txt, type));
					}
					return Result.empty();
				});

			}
		};
	}

	/**
	 * Tokenizer that matches a literal String.<br>
	 * The string must start and end with the supplied stringDelimiter char.<br>
	 * String can contain escaped chars like in java/javascript: \t, \\, \b, \r, \n, \/
	 * and can contain unicode chars in the form of \\uXXXX where XXXX is a hexadecimal number.
	 *
	 * @param type            The resulting token Type
	 * @param stringDelimiter The string start/end character
	 * @param multiLine       If the string can span multiple lines
	 * @param <TT>            The Type of the Token
	 *
	 * @return A TokenMatcher
	 */
	@SuppressWarnings("BooleanParameter")
	public static <TT> TokenMatcher<TT> stringMatcher(TT type, char stringDelimiter, boolean multiLine) {
		return code ->
			Result.function(code, type, stringDelimiter, multiLine).code(log -> {
				if(code == null) {
					return Result.failure("code is null");
				}
				int  i     = 0;
				char start = code.charAt(i++);
				if(start != stringDelimiter) {
					return Result.empty();
				}
				StringBuilder sb = new StringBuilder(10);
				try {
					sb.append(start);
					char c = code.charAt(i);
					while(c != start && (multiLine == true || (c != '\n'))) {
						if(c == '\\') {
							c = code.charAt(++i);//skip \
							switch(c) {
								case '\\':
									c = code.charAt(++i);
									sb.append('\\');
									break;
								case '\"':
									c = code.charAt(++i);
									sb.append('\"');
									break;
								case '\'':
									c = code.charAt(++i);
									sb.append('\'');
									break;
								case 'b':
									c = code.charAt(++i);
									sb.append('\b');
									break;
								case 'r':
									c = code.charAt(++i);
									sb.append('\r');
									break;
								case 'n':
									c = code.charAt(++i);
									sb.append('\n');
									break;
								case 't':
									c = code.charAt(++i);
									sb.append('\t');
									break;
								case '/':
									c = code.charAt(++i);
									sb.append('/');
									break;
								case 'u':
									c = code.charAt(++i);//skip u
									String hn = Character.toString(c);
									c = code.charAt(++i);
									hn += c;
									c = code.charAt(++i);
									hn += c;
									c = code.charAt(++i);
									hn += c;
									c = code.charAt(++i);
									sb.append(Character.toChars(Integer.parseInt(hn, 16)));
									break;
								default:
									return Result.failure("Invalid escape sequence: \\" + c);
							}
						}
						else {

							sb.append(c);
							c = code.charAt(++i);
						}
					}
					++i;
					return Result.success(new TokenFound<>(sb.append(start).toString(), type, false, i));

				} catch(StringIndexOutOfBoundsException e) {
					return Result.failure(new RuntimeException("Unclosed string", e));
				}

			});

	}

	/**
	 * Shortcut for add(regExMatcher(regex,type)
	 *
	 * @param regex The Regular Expression to add
	 * @param type  The resulting type if there is a match
	 *
	 * @return this
	 */
	public SimpleTokenizer<TT> add(String regex, TT type) {
		return add(regExMatcher(regex, type));
	}

	/**
	 * Tokenize the given code with the given name.<br>
	 *
	 * @param name       The name of the code or source file
	 * @param sourceCode The code as a string
	 *
	 * @return A Result with a list of {@link Token}s corresponding to the source file.
	 */
	public Result<PList<Token<TT>>> tokenizeToResult(String name, String sourceCode) {
		return Result.fromSequence(
			tokenize(name, sourceCode))
			.filter(t -> t.isEmpty() == false)
			.map(p -> p.plist()
			);
	}


	/**
	 * Parse the sourceCode and generate a lazy PStream of tokens.<br>
	 * Every PStream is ended with a Result.empty to indicate the end of the stream.
	 *
	 * @param name       The name of the source code
	 * @param sourceCode The source code
	 *
	 * @return Lazy PStream of token results.
	 */
	public PStream<Result<Token<TT>>> tokenize(String name, String sourceCode) {
		return Log.function(name, sourceCode).code(log -> {
			Pos pos = new Pos(name, 1, 1);
			if(name == null) {
				return PStream.val(Result.failure("name for the code is null"));
			}
			if(sourceCode == null) {
				return PStream.val(Result.failure("The source code  is null"));
			}
			if(sourceCode.isEmpty()) {
				return PList.val(Result.empty("No Source"));
			}
			Result<Tuple3<String, Pos, TokenFound<TT>>> initToken = processNextToken(pos, sourceCode);

			PStream<Result<Token<TT>>> resultStream = PStream
				//Make a sequence using the Previews processed result
				.sequence(initToken,
						  prevResult -> prevResult.flatMap(prv -> processNextToken(prv._2, prv._1))
				)
				//Filter all ignored tokens
				.filter(pr -> pr.map(t -> t._3.ignore == false).orElse(true))
				//.filter(pr -> pr.isEmpty() == false)
				//.peek(t -> System.out.println(t.map(t3 -> "" + t3._2 + ", " + t3._3)))
				//Limit to Result.empty or Result.failure
				.limitOnPreviousValue(prev ->
										  prev.isPresent() == false
				)
				//Convert to the result type
				//.peek(t -> System.out.println(t.map(t3 -> "" + t3._2 + ", " + t3._3)))
				.map(pr -> pr.map(t3 -> new Token<>(t3._2, t3._3.type, t3._3.text)));
			return resultStream;
		});
	}

	private Result<Tuple3<String, Pos, TokenFound<TT>>> processNextToken(Pos thisPos, String code) {
		return findToken(code)
			.flatMapFailure(f -> Result.failure(new TokenizerException(thisPos, f.getException())))
			.verify(found -> found.skipLength != 0,
					found -> new TokenizerException(thisPos, "Found a match with length 0. Type=" + found.type)
			)
			.map(found -> {
				String skipString = code.substring(0, found.skipLength);
				int    nlCount    = newLineCount(skipString);
				Pos    newPos     = thisPos;
				if(nlCount > 0) {
					int lastNl = skipString.lastIndexOf('\n');
					newPos = newPos.withLineNumber(newPos.lineNumber + nlCount);
					newPos = newPos.withColumn(found.skipLength - lastNl);
				}
				else {
					newPos = newPos.withColumn(newPos.column + found.skipLength);
				}
				String newCode = code.substring(found.skipLength);
				return Tuple3.of(newCode, newPos, found);
			});

	}

	private Result<TokenFound<TT>> findToken(String code) {
		return Result.function().code(log -> {
			if(code.isEmpty()) {
				return Result.empty("code string is empty");
			}
			for(TokenMatcher<TT> sup : tokenMatchers) {
				Result<TokenFound<TT>> result = sup.tryParse(code);
				if(result.isPresent() || result.isError()) {
					return result;
				}
			}
			return Result.failure("Unknown token:" + code);
		});
	}

	private int newLineCount(String txt) {
		int count = 0;
		for(int i = 0; i < txt.length(); i++) {
			if(txt.charAt(i) == '\n') {
				count++;
			}
		}
		return count;
	}

}
