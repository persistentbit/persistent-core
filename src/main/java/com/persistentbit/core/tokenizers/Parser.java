package com.persistentbit.core.tokenizers;

import com.persistentbit.core.result.Result;

import java.util.Iterator;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/02/2017
 */
public class Parser<TT> {
    private final Iterator<Token<TT>> iter;
    protected Token<TT>   currentToken;
    protected TT current;
    protected String currentText;


    public Parser(Iterator<Token<TT>> tokensIterator){
        this.iter = tokensIterator;
        next();
    }
    protected TT next(){
        if(iter.hasNext() == false){
            throw new IllegalStateException("No more tokens after " + currentToken);
        }
        currentToken = iter.next();
        if(currentToken.result.leftOpt().isPresent()){
            //We have a tokenizer error
            throw new IllegalStateException(currentToken.result.leftOpt().get());
        }
        current = currentToken.result.rightOpt().get().type;
        currentText = currentToken.result.rightOpt().get().text;
        return current;
    }
    protected String textAndNext(){
        String text = currentText;
        next();
        return text;
    }
    protected Result<Token<TT>> parseTokenType(TT tokenType, String errorMessage){
        return Result.function().code(l -> {
            if(current != tokenType){
                return error(errorMessage);
            }
            Token<TT> res = currentToken;
            next();
            return Result.success(res);
        });
    }
    protected <X> Result<X> error(Pos pos, String msg){
        return Result.failure(new ParserException(pos, msg));
    }
    protected <X> Result<X> error(String msg){
        return error(currentToken.pos,msg);
    }
}
