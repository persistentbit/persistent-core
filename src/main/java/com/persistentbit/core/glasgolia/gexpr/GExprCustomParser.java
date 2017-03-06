package com.persistentbit.core.glasgolia.gexpr;

import com.persistentbit.core.parser.Parser;

/**
 * TODOC
 *
 * @author petermuys
 * @since 6/03/17
 */
@FunctionalInterface
public interface GExprCustomParser{

	Parser<GExpr> getParser(GExprParser mainParser);

}
