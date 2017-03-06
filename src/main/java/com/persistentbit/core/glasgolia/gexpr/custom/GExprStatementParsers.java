package com.persistentbit.core.glasgolia.gexpr.custom;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.glasgolia.ETypeSig;
import com.persistentbit.core.glasgolia.gexpr.GExpr;
import com.persistentbit.core.glasgolia.gexpr.GExprCustomParser;
import com.persistentbit.core.glasgolia.gexpr.GExprParser;
import com.persistentbit.core.parser.Parser;

/**
 * TODOC
 *
 * @author petermuys
 * @since 6/03/17
 */
public class GExprStatementParsers implements GExprCustomParser{

	public GExprCustomParser ifParser = mainParser -> {
		return mainParser.keyword("if")
						 .skipAnd(mainParser.parseExpr().and(mainParser.parseExpr()))
						 .and(
							 mainParser.keyword("else")
									   .skipAnd(mainParser.parseExpr())
									   .optional()
						 )
						 .map(t -> {
							 if(t._2.isPresent()) {
								 return new GExpr.Custom(
									 t._1._1.getPos(),
									 "ifElse",
									 PList.val(t._1._1, t._1._2, t._2.get()), t._1._2.getType()
								 );
							 }
							 else {
								 return new GExpr.Custom(
									 t._1._1.getPos(),
									 "if",
									 PList.val(t._1._1, t._1._2), t._1._2.getType()
								 );
							 }
						 });
	};

	public GExprCustomParser importParser = mainParser -> {
		return
			mainParser.keyword("import")
					  .skipAnd(mainParser.parseStringLiteral.withPos())
					  .and(mainParser.keyword("as").skipAnd(mainParser.parseName).optional())
					  .map(t -> {
						  return new GExpr.Custom(
							  t._1.pos,
							  "import",
							  PList.val(t._1.value, t._2.orElse(null)), new ETypeSig.Cls(String.class)
						  );
					  });

	};

	public GExprCustomParser whileParser = mainParser ->
		mainParser.keyword("while")
				  .skipAnd(mainParser.parseExpr())
				  .and(mainParser.parseExpr())
				  .map(t ->
					  new GExpr.Custom(
						  t._1.getPos(),
						  "while",
						  PList.val(t._1, t._2),
						  t._2.getType()
					  )
				  );
	public GExprCustomParser sourceParser = mainParser ->
		mainParser.keyword("source")
				  .skipAnd(mainParser.parseStringLiteral)
				  .map(t -> new GExpr.Custom(
					  t.getPos(),
					  "source",
					  PList.val(t),
					  t.getType()
				  ));

	public GExprCustomParser classPathParser = mainParser ->
		mainParser.keyword("classpath")
				  .skipAnd(mainParser.parseStringLiteral)
				  .map(t -> new GExpr.Custom(
					  t.getPos(),
					  "classpath",
					  PList.val(t), new ETypeSig.Cls(String.class)
				  ));


	@Override
	public Parser<GExpr> getParser(GExprParser mainParser
	) {
		return ifParser.getParser(mainParser)
					   .or(importParser.getParser(mainParser))
					   .or(whileParser.getParser(mainParser))
					   .or(sourceParser.getParser(mainParser))
					   .or(classPathParser.getParser(mainParser));

	}
}
