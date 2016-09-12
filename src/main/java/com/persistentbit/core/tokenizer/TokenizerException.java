package com.persistentbit.core.tokenizer;

/**
 * Created by petermuys on 12/09/16.
 */
public class TokenizerException extends RuntimeException{
    private final SimpleTokenizer.Pos pos;

    public TokenizerException(SimpleTokenizer.Pos pos,String message) {
        super(asMessage(pos,message));
        this.pos = pos;
    }
    private static String asMessage(SimpleTokenizer.Pos pos,String msg){
        return "Tokenizer error in '" + pos.name + "' at (" + pos.lineNumber + "," + pos.column +"): "+ msg;
    }

    public TokenizerException(SimpleTokenizer.Pos pos,String message, Throwable cause) {
        super(asMessage(pos,message),cause);
        this.pos = pos;
    }

    public TokenizerException(SimpleTokenizer.Pos pos,Throwable cause) {
        this(pos,asMessage(pos,cause.getMessage()),cause);
    }

}
