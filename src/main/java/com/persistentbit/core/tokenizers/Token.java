package com.persistentbit.core.tokenizers;

import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.Either;

/**
 * A token generated by a Tokenizer like {@link SimpleTokenizer}.
 *
 * @author Peter Muys
 */
public class Token<TT> extends BaseValueClass {
    public static class Data<TT> extends BaseValueClass {
        public final TT type;
        public final String text;

        public Data(TT type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    /**
     * The position of the token in the file
     */
    public final Pos pos;
    /**
	 * The result orOf an error message
	 */
    public final Either<String,Data<TT>>    result;

    private Token(Pos pos, Either<String,Data<TT>> result){
        this.pos = pos;
        this.result = result;
    }

    public Token(Pos pos, TT type, String text) {
        this(pos,Either.right(new Data<>(type,text)));
    }
    public Token(Pos pos, String error){
        this(pos,Either.left(error));
    }

    @Override
    public String toString() {
        return "Token{" +
                "pos=" + pos +
                ", result=" + result +
                '}';
    }
}
