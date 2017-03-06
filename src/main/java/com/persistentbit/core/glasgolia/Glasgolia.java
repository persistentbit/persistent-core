package com.persistentbit.core.glasgolia;

import com.persistentbit.core.easyscript.EExpr;
import com.persistentbit.core.easyscript.EParser;
import com.persistentbit.core.function.Memoizer;
import com.persistentbit.core.glasgolia.compiler.CompileGToR;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
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
public class Glasgolia{

	private final ResourceLoader            resourceLoader;
	private final CompileGToR                     compiler;
	private Function<String, Result<RExpr>> compiled;

	public Glasgolia(CompileGToR compiler,ResourceLoader resourceLoader) {
		this.compiler = compiler;
		this.resourceLoader = resourceLoader;
		this.compiled = Memoizer.of(name ->
			resourceLoader.apply(name)
						  .map(pb -> pb.toText(IO.utf8))
				          .flatMap(code -> compiler.compile(Source.asSource(name,code)))
		);
	}

	public Glasgolia() {
		this(new CompileGToR(),ClassPathResourceLoader.inst);
	}

	private Result<EExpr> parse(Source source) {
		return EParser.ws.skipAnd(EParser.parseExprList())//.skip(Scan.eof)
						 .parse(source)
						 .asResult();
	}

	private <T> Result<T> eval(RExpr expr) {
		try {
			return Result.result((T) expr.get());
		} catch(Exception e) {
			return Result.failure(e);
		}
	}

	public <T> Result<T> loadAndEval(String sourceName) {
		return compiled.apply(sourceName).flatMap(this::eval);
	}

	public <T> Result<T> eval(String name, String code) {
		return compiler.compile(Source.asSource(name, code))
			.flatMap(this::eval);
	}
}
