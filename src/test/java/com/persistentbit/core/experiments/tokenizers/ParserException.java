package com.persistentbit.core.experiments.tokenizers;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/02/2017
 */
public class ParserException extends RuntimeException{
    private final Pos pos;

    public ParserException(Pos pos, String message){
        super(message);
        this.pos = pos;
    }

    public Pos getPos() {
        return pos;
    }
}
