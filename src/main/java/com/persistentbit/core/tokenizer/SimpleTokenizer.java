package com.persistentbit.core.tokenizer;

import com.persistentbit.core.Tuple2;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by petermuys on 12/09/16.
 */
public class SimpleTokenizer<TT> {
    static public class Pos extends BaseValueClass {
        public final String name;
        public final int lineNumber;
        public final int column;
        public Pos(String name, int lineNumber,int column){
            this.name = name;
            this.lineNumber = lineNumber;
            this.column = column;
        }
    }
    static public class Token<TT> extends BaseValueClass{
        public final Pos pos;
        public final TT  type;
        public final String text;

        public Token(Pos pos, TT type, String text) {
            this.pos = pos;
            this.type = type;
            this.text = text;
        }
    }

    @FunctionalInterface
    public interface TokenSupplier<TT>{
        Optional<Tuple2<String,TT>> apply(String code);
    }



    private PList<TokenSupplier<TT>> tokenSuppliers = PList.empty();

    public SimpleTokenizer<TT> add(TokenSupplier<TT> tokenSupplier){
        tokenSuppliers = tokenSuppliers.plus(tokenSupplier);
        return this;
    }

    public SimpleTokenizer<TT> add(String regex, TT type) {
        return add(new TokenSupplier<TT>() {
            private Pattern pattern = Pattern.compile("\\A("+regex+")",Pattern.DOTALL | Pattern.MULTILINE);
            @Override
            public Optional<Tuple2<String, TT>> apply(String code) {
                Matcher m = pattern.matcher(code);
                if (m.find()) {
                    String txt = m.group();
                    return Optional.of(new Tuple2<>(txt,type));
                }
                return Optional.empty();
            }
        });

    }

    public PList<Token<TT>> tokenize(String name, String code){
        int line = 1;
        int col  = 1;
        PList<Token<TT>> res = PList.empty();
        while(code.isEmpty() == false){
            Tuple2<String, TT> found;
            try {
                found = findToken(code);
            } catch (Exception e){
                throw new TokenizerException(new Pos(name,line,col),e);
            }
            if(found == null){
                throw new TokenizerException(new Pos(name,line,col),"Unrecognized token.");
            }
            if(found._1.length() == 0){
                throw new TokenizerException(new Pos(name,line,col),"Found a match with length 0 at ");
            }
            res = res.plus(new Token<>(new Pos(name,line,col),found._2,found._1));
            int nlCount = newLineCount(found._1);
            if(nlCount > 0){
                int lastNl = found._1.lastIndexOf('\n');
                line += nlCount;
                col = found._1.length() - lastNl;
            } else {
                col += found._1.length();
            }
            code = code.substring(found._1.length());
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


    static public <TT> TokenSupplier<TT> stringSupplier(TT token,char stringDelimiter, boolean multiLine) {
        return (code -> {
            StringBuilder sb = new StringBuilder(10);
            try{
                int i = 0;
                char start = code.charAt(i++);
                if(start != stringDelimiter){
                    return Optional.empty();
                }
                sb.append(start);
                char c = code.charAt(i);
                while(c != start  && (multiLine == true || (c != '\n'))){
                    if(c == '\\'){
                        c = code.charAt(++i);//skip \
                        switch(c){
                            case '\\': c=code.charAt(++i); sb.append('\\');break;
                            case '\"': c=code.charAt(++i); sb.append('\"');break;
                            case '\'': c=code.charAt(++i); sb.append('\"');break;
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
                return Optional.of(new Tuple2<String, TT>(sb.append(start).toString(),token));

            }catch(StringIndexOutOfBoundsException e){
                throw new RuntimeException("Unclosed string");
            }
        });



    }

    private Tuple2<String,TT> findToken(String code){
        for(TokenSupplier<TT> sup : tokenSuppliers){
            Tuple2<String,TT> result = sup.apply(code).orElse(null);
            if(result != null){
                return result;
            }
        }
        return null;
    }
    static public void main(String...args){
        SimpleTokenizer<Integer> tokenizer = new SimpleTokenizer<>();
        tokenizer.add("(\\s)+",-1);
        tokenizer.add(SimpleTokenizer.stringSupplier(-2,'\"',false));
        tokenizer.add(SimpleTokenizer.stringSupplier(-2,'\'',false));
        tokenizer.add("sin|cos|exp|ln|sqrt", 1); // function
        tokenizer.add("/\\*.*\\*/",-9); //comment
        tokenizer.add("\\(", 2); // open bracket
        tokenizer.add("\\)", 3); // close bracket
        tokenizer.add("[+-]", 4); // plus or minus
        tokenizer.add("[*/]", 5); // mult or divide
        tokenizer.add("\\^", 6); // raised
        tokenizer.add("[0-9]+",7); // integer number
        tokenizer.add("[a-zA-Z][a-zA-Z0-9_]*", 8); // variable

        String txt = " sin(' x') * /*(1+\n" +
                "var_12)*/test";
        tokenizer.tokenize("test",txt).forEach(System.out::println);
    }

}
