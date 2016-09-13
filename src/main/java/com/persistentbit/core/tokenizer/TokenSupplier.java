package com.persistentbit.core.tokenizer;

import java.util.function.Function;

/**
 * A token supplier tries to parse the given string into a Token.<br>
 * If it succeeds, it will return a new TokenFound instance.<br>
 *
 * @author Peter Muys
 * @see SimpleTokenizer
 */
@FunctionalInterface
public interface TokenSupplier<TT> {
    /**
     * Try parsing the beginning of the supplied code.<br>
     * If this supplier can parse the code, return a new {@link TokenFound} instance,
     * else just return null.<br>
     * @param code The code to parse
     * @return null if not able to parse or a valid TokenFound instance.
     */
    TokenFound<TT>  tryParse(String code);

    /**
     * Add a mapper function to this TokenSupplier that transforms the
     * @param mapper The mapping function. Will only be called if there is a token found
     * @return This mapped token supplier.
     */
    default TokenSupplier<TT> map(Function<TokenFound<TT>,TokenFound<TT>> mapper){
        return code -> {
            TokenFound<TT> found = TokenSupplier.this.tryParse(code);
            if(found != null) {
                return mapper.apply(found);
            }
            return null;
        };
    }

    /**
     * returns a Supplier that transforms the found Token to a token that should be ignored.
     * @return The new TokenSupplier
     */
    default TokenSupplier<TT> ignore() {
        return this.map(found ->  new TokenFound<>(found.text,found.type,false));
    }
}
