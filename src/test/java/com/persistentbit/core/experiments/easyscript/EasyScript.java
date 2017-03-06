package com.persistentbit.core.experiments.easyscript;

import com.persistentbit.core.function.Memoizer;
import com.persistentbit.core.parser.Scan;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.resources.ClassPathResourceLoader;
import com.persistentbit.core.resources.ResourceLoader;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.IO;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 27/02/17
 */
public class EasyScript{

	private final ResourceLoader resourceLoader;
	private EvalContext context = EvalContext.inst;
	private Function<String, Result<EExpr>> parsed;

	public EasyScript(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
		this.parsed = Memoizer.of(name ->
			resourceLoader.apply(name)
						  .map(pb -> pb.toText(IO.utf8))
						  .flatMap(code -> parse(Source.asSource(name, code)))
		);
		ECallable println = (args) -> {
			System.out.println(args[0]);
			return null;
		};
		context = context.withValue("println", println);
	}

	public EasyScript() {
		this(ClassPathResourceLoader.inst);
	}

	private Result<EExpr> parse(Source source) {
		return EParser.ws.skipAnd(EParser.parseExprList()).skip(Scan.eof)
						 .parse(source)
						 .asResult();
	}

	private <T> Result<T> eval(EExpr expr) {
		try {
			EEvalResult res = EEvaluator.inst.evalExpr(context, expr);
			context = res.getContext();

			return Result.result((T) res.getValue());
		} catch(Exception e) {
			return Result.failure(e);
		}
	}

	public <T> Result<T> loadAndEval(String sourceName) {
		return parsed.apply(sourceName)
					 .flatMap(this::eval);
	}

	public <T> Result<T> eval(String name, String code) {
		return parse(Source.asSource(name, code))
			.flatMap(this::eval);
	}
}
