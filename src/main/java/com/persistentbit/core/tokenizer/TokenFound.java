package com.persistentbit.core.tokenizer;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * A TokenFound is the result of a {@link TokenMatcher} instance.
 *
 * @author Peter Muys
 */
public class TokenFound<TT> extends BaseValueClass{
    public final String text;
    public final TT type;
    public final boolean ignore;

    public TokenFound(String text, TT type){
        this(text,type,false);
    }
    public TokenFound(String text, TT type, boolean ignore) {
        this.text = text;
        this.type = type;
        this.ignore = ignore;
    }
}
