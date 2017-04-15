package com.persistentbit.core.tokenizers;

import com.persistentbit.core.io.IORead;
import com.persistentbit.core.logging.Log;

import java.io.Reader;
import java.util.Iterator;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/02/2017
 */
public interface Tokenizer<TT> {

    Iterator<Token<TT>> tokenize(String name, String code);

    default Iterator<Token<TT>> tokenize(String name, Reader reader){
        return Log.function(name).code(l ->
           tokenize(name, IORead.readTextStream(reader).orElseThrow())
        );
    }

}
