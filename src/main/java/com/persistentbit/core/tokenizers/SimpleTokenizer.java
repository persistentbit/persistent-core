package com.persistentbit.core.tokenizers;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.StringUtils;

import java.util.Iterator;
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
public class SimpleTokenizer<TT> implements Tokenizer<TT> {

    private final TT endOfFileToken;
    private final PList<TokenMatcher<TT>> tokenMatchers;


    private SimpleTokenizer(TT endOfFileToken, PList<TokenMatcher<TT>> tokenMatchers) {
        this.endOfFileToken = Objects.requireNonNull(endOfFileToken);
        this.tokenMatchers = Objects.requireNonNull(tokenMatchers);
    }

    public SimpleTokenizer(TT endOfFileToken) {
        this(endOfFileToken, PList.empty());
    }

    /**
     * Add a token matcher to the list of matchers
     *
     * @param tokenMatcher The matcher to add
     * @return this.
     */
    public SimpleTokenizer<TT> add(TokenMatcher<TT> tokenMatcher) {
        return Log.function(tokenMatcher).code(l ->
                new SimpleTokenizer<>(endOfFileToken, tokenMatchers.plus(Objects.requireNonNull(tokenMatcher)))
        );
    }

    /**
     * A Token matcher that uses a Regular Expression to match the token.
     *
     * @param regex The regular Expression to match
     * @param type  The resulting token type
     * @param <TT>  The Type of the Token Type
     * @return The TokenMatcher
     */
    public static <TT> TokenMatcher<TT> regExMatcher(String regex, TT type) {
        return new TokenMatcher<TT>() {
            private final Pattern pattern = Pattern.compile("\\A(" + regex + ")", Pattern.DOTALL | Pattern.MULTILINE);

            @Override
            public Result<TokenFound<TT>> tryParse(String code) {
                return Result.function(code).code(l -> {
                    if (code == null) {
                        return Result.failure("code is null");
                    }
                    Matcher m = pattern.matcher(code);
                    if (m.find()) {
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
     * @return A TokenMatcher
     */
    @SuppressWarnings("BooleanParameter")
    public static <TT> TokenMatcher<TT> stringMatcher(TT type, char stringDelimiter, boolean multiLine) {
        return code ->
                Result.function(code, type, stringDelimiter, multiLine).code(log -> {
                    if (code == null) {
                        return Result.failure("code is null");
                    }
                    int i = 0;
                    char start = code.charAt(i++);
                    if (start != stringDelimiter) {
                        return Result.empty();
                    }
                    StringBuilder sb = new StringBuilder(10);
                    try {
                        sb.append(start);
                        char c = code.charAt(i);
                        while (c != start && (multiLine == true || (c != '\n'))) {
                            if (c == '\\') {
                                c = code.charAt(++i);//skip \
                                switch (c) {
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
                            } else {

                                sb.append(c);
                                c = code.charAt(++i);
                            }
                        }
                        ++i;
                        return Result.success(new TokenFound<>(sb.append(start).toString(), type, false, i));

                    } catch (StringIndexOutOfBoundsException e) {
                        return Result.failure(new RuntimeException("Unclosed string", e));
                    }

                });

    }

    /**
     * Shortcut for add(regExMatcher(regex,type)
     *
     * @param regex The Regular Expression to add
     * @param type  The resulting type if there is a match
     * @return this
     */
    public SimpleTokenizer<TT> add(String regex, TT type) {
        return add(regExMatcher(regex, type));
    }


    private class TokenIterator implements Iterator<Token<TT>> {
        private final String name;
        private int line = 1;
        private int col = 1;

        private String code;
        private Token<TT> nextToken;
        private boolean hasShownEnd = false;
        private boolean hasNext = true;

        public TokenIterator(String name, String code) {
            this.name = name;
            this.code = code;
            doNext();
        }

        private void doNext() {
            if (hasNext == false) {
                return;
            }
            if (nextToken != null) {
                if (nextToken.result.leftOpt().isPresent()) {
                    //If previous was an error, then we stop now.
                    hasNext = false;
                    nextToken = null;
                    return;
                }
            }
            boolean done;
            do {
                if (code.isEmpty()) {
                    //If we are at the end of the file
                    if (hasShownEnd == false) {
                        //First show end of file token
                        nextToken = new Token<>(new Pos(name, line, col), endOfFileToken, "");
                        hasShownEnd = true;
                        return;
                    }
                    //We are done.
                    hasNext = false;
                    return;
                }
                Pos pos = new Pos(name, line, col);
                Result<TokenFound<TT>> resFound = findToken(code);
                if (resFound.isEmpty()) {
                    nextToken = new Token<>(pos, "Unknown token");
                    return;
                }
                if (resFound.isError()) {
                    nextToken = new Token<>(pos, resFound.getEmptyOrFailureException().get().getMessage());
                    return;
                }
                TokenFound<TT> found = resFound.orElseThrow();
                int len = found.skipLength;
                if (len == 0) {
                    nextToken = new Token<>(pos, "Found a match with length 0. Type=" + found.type);
                    return;
                }
                String skipString = code.substring(0, len);
                int nlCount = StringUtils.countCharOccurrence(skipString, '\n');

                if (nlCount > 0) {
                    int lastNl = skipString.lastIndexOf('\n');
                    line += nlCount;
                    col = len - lastNl;
                } else {
                    col += len;
                }
                code = code.substring(len);
                if (found.ignore) {
                    done = false;
                } else {
                    done = true;
                    nextToken = new Token<>(new Pos(name, line, col), found.type, found.text);
                }
            } while (done == false);
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Token<TT> next() {
            if (hasNext == false) {
                throw new IllegalStateException("There is no next!");
            }
            Token<TT> result = nextToken;
            doNext();
            return result;
        }
    }

    /**
     * Tokenize the given string, ending with the end-of-file token.
     *
     * @param name The name of the source code
     * @param code The code
     * @return An iterator getting the tokens in the string or
     * an error code.
     */
    @Override
    public Iterator<Token<TT>> tokenize(String name, String code) {
        return new TokenIterator(name, code);
    }

    private Result<TokenFound<TT>> findToken(String code) {
        return Result.function().code(log -> {
            for (TokenMatcher<TT> sup : tokenMatchers) {
                Result<TokenFound<TT>> result = sup.tryParse(code);
                if (result.isPresent()) {
                    return result;
                }
            }
            return Result.empty("Unknown token:" + code);
        });
    }


}
