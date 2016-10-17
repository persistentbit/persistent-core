package com.persistentbit.core.tokenizer;

import com.persistentbit.core.collections.PList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Simple Tokenizer (Lexer) can transform your text into tokens.<br>
 * You add token parser by adding {@link TokenMatcher}s to the tokenizer.<br>
 * The way this works is that the tokenizer loops throue the matchers one by one and if a token is found,
 * then this is added to the result {@link Token} list.<br>
 * This tokenizer is not build for speed, but for easy usage. If you want to have more speed, you can still build a lexer
 * by hand.<br>
 * @author Peter Muys
 * @see Token
 * @see TokenMatcher
 * @see TokenFound
 */
public class SimpleTokenizer<TT> {




    private PList<TokenMatcher<TT>> tokenMatchers = PList.empty();

    /**
     * Add a token matcher to the list of matchers
     * @param tokenMatcher The matcher to add
     * @return this.
     */
    public SimpleTokenizer<TT> add(TokenMatcher<TT> tokenMatcher){
        tokenMatchers = tokenMatchers.plus(tokenMatcher);
        return this;
    }

    /**
     * Shortcut for add(regExMatcher(regex,type)
     * @param regex The Regular Expression to add
     * @param type The resulting type if there is a match
     * @return this
     */
    public SimpleTokenizer<TT> add(String regex, TT type) {
        return add(regExMatcher(regex,type));
    }

    /**
     * Tokenize the given code with the given name.<br>
     * @param name The name of the code or source file
     * @param code The code as a string
     * @return A list of {@link Token}s corresponding to the source file.
     * @throws TokenizerException Thrown when any exception occurs during the conversion.
     */
    public PList<Token<TT>> tokenize(String name, String code) throws TokenizerException{
        int line = 1;
        int col  = 1;
        PList<Token<TT>> res = PList.empty();
        while(code.isEmpty() == false){
            TokenFound<TT> found;
            try {
                found = findToken(code);
            } catch (Exception e){
                throw new TokenizerException(new Pos(name,line,col),e);
            }
            if(found == null){
                throw new TokenizerException(new Pos(name,line,col),"Unrecognized token.");
            }
            int len = found.skipLength;
            if(len == 0){
                throw new TokenizerException(new Pos(name,line,col),"Found a match with length 0. Type=" + found.type);
            }
            if(found.ignore == false) {
                res = res.plus(new Token<>(new Pos(name, line, col), found.type, found.text));
            }
            String skipString = code.substring(0,len);
            int nlCount = newLineCount(skipString);

            if(nlCount > 0){
                int lastNl = skipString.lastIndexOf('\n');
                line += nlCount;
                col = len - lastNl;
            } else {
                col += len;
            }
            code = code.substring(len);
        }
        return res;
    }
    private int newLineCount(String txt){
        int count = 0;
        for(int i=0; i<txt.length();i++){
            if(txt.charAt(i) == '\n'){
                count++;
            }
        }
        return count;
    }

    /**
     * A Token matcher that uses a Regular Expression to match the token.
     * @param regex The regular Expression to match
     * @param type The resulting token type
     * @param <TT> The Type of the Token Type
     * @return The TokenMatcher
     */
    static public <TT> TokenMatcher<TT> regExMatcher(String regex, TT type){
        return new TokenMatcher<TT>() {
            private Pattern pattern = Pattern.compile("\\A("+regex+")",Pattern.DOTALL | Pattern.MULTILINE);

            @Override
            public TokenFound<TT> tryParse(String code) {
                Matcher m = pattern.matcher(code);
                if (m.find()) {
                    String txt = m.group();
                    return new TokenFound<>(txt,type);
                }
                return null;
            }
        };
    }


    /**
     * Tokenizer that matches a literal String.<br>
     * The string must start and end with the supplied stringDelimiter char.<br>
     * String can contain escaped chars like in java/javascript: \t, \\, \b, \r, \n, \/
     * and can contain unicode chars in the form of \\uXXXX where XXXX is a hexadecimal number.
     * @param type The resulting token Type
     * @param stringDelimiter    The string start/end character
     * @param multiLine If the string can span multiple lines
     * @param <TT> The Type of the Token
     * @return A TokenMatcher
     */
    static public <TT> TokenMatcher<TT> stringMatcher(TT type, char stringDelimiter, boolean multiLine) {
        return (code -> {

            StringBuilder sb = new StringBuilder(10);
            int i = 0;
            try{
                char start = code.charAt(i++);
                if(start != stringDelimiter){
                    return null;
                }
                sb.append(start);
                char c = code.charAt(i);
                while(c != start  && (multiLine == true || (c != '\n'))){
                    if(c == '\\'){
                        c = code.charAt(++i);//skip \
                        switch(c){
                            case '\\': c=code.charAt(++i); sb.append('\\');break;
                            case '\"': c=code.charAt(++i); sb.append('\"');break;
                            case '\'': c=code.charAt(++i); sb.append('\'');break;
                            case 'b': c=code.charAt(++i); sb.append('\b');break;
                            case 'r': c=code.charAt(++i); sb.append('\r');break;
                            case 'n': c=code.charAt(++i); sb.append('\n');break;
                            case 't': c=code.charAt(++i); sb.append('\t');break;
                            case '/': c=code.charAt(++i); sb.append('/');break;
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
                                sb.append(Character.toChars(Integer.parseInt(hn,16)));
                                break;
                            default:
                                throw new RuntimeException("Invalid escape sequence: \\" + c );
                        }
                    } else {

                        sb.append(c);
                        c = code.charAt(++i);
                    }
                }
                code.charAt(++i);
                return new TokenFound<>(sb.append(start).toString(),type,false,i);

            }catch(StringIndexOutOfBoundsException e){
                throw new RuntimeException("Unclosed string");
            }
        });



    }

    private TokenFound<TT> findToken(String code){
        for(TokenMatcher<TT> sup : tokenMatchers){
            TokenFound<TT> result = sup.tryParse(code);
            if(result != null){
                return result;
            }
        }
        return null;
    }

    static public void main(String...args){
        SimpleTokenizer<Integer> tokenizer = new SimpleTokenizer<>();
        tokenizer.add(SimpleTokenizer.regExMatcher("(\\s)+",-1).ignore());
        tokenizer.add(SimpleTokenizer.stringMatcher(-2,'\"',false));
        tokenizer.add(SimpleTokenizer.stringMatcher(-2,'\'',false));
        tokenizer.add("/\\*.*\\*/",-9); //comment
        tokenizer.add("\\(", 2); // open bracket
        tokenizer.add("\\)", 3); // close bracket
        tokenizer.add("[+-]", 4); // plus or minus
        tokenizer.add("[*/]", 5); // mult or divide
        tokenizer.add("\\^", 6); // raised
        tokenizer.add("[0-9]+",7); // integer number
        tokenizer.add(SimpleTokenizer.regExMatcher("[a-zA-Z][a-zA-Z0-9_]*", 8).map(found -> {
            switch(found.text){
                case "sin":
                case "cos":
                    return new TokenFound<Integer>(found.text,1,found.ignore);
                default: return found;
            }
        })); // variable

        String txt = " sin(' x') * /*(1+\n" +
                "var_12)\n" +
                "*/test";
        tokenizer.tokenize("test",txt).forEach(System.out::println);
    }

}
