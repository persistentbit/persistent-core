package com.persistentbit.core.experiments.tokenizers;

import com.persistentbit.core.tokenizers.SimpleTokenizer;
import com.persistentbit.core.tokenizers.Token;
import com.persistentbit.core.tokenizers.Tokenizer;

import java.util.Iterator;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/02/2017
 */
public class ExprTokenizer {

    static private Tokenizer<ExprToken> tokenizer = new SimpleTokenizer<>(ExprToken.eof)
            .add(SimpleTokenizer.regExMatcher("\\n", ExprToken.ignored).ignore())
            .add("\\(", ExprToken.tOpen)
            .add("\\)", ExprToken.tClose)
            .add(SimpleTokenizer.regExMatcher("/\\*.*?\\*/", ExprToken.ignored).ignore())
            .add("\\\\", ExprToken.tLambda)
            .add("\\.", ExprToken.tPoint)
            .add("[a-zA-Z]+", ExprToken.tName)
    ;

    static public Iterator<Token<ExprToken>>  tokenize(String name, String code){
        return tokenizer.tokenize(name,code);
    }
}
