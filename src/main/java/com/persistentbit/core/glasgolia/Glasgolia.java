package com.persistentbit.core.glasgolia;

/**
 * TODOC
 *
 * @author petermuys
 * @since 27/02/17
 */
public class Glasgolia{
/*
	private final ResourceLoader            resourceLoader;
	private final CompileGToR                     compiler;
	private Function<String, Result<RExpr>> compiled;
	private PList<String> loaded = PList.empty();

	public Glasgolia(CompileGToR compiler,ResourceLoader resourceLoader) {
		this.compiler = compiler;
		this.resourceLoader = resourceLoader;
		this.compiled = Memoizer.of(name ->
			resourceLoader.apply(name)
						  .map(pb -> pb.toText(IO.utf8))
				          .flatMap(code -> {
				          	return compiler.compile(Source.asSource(name,code))
				          	  .verify(r -> compiler.getUndefinedVars().isEmpty() ,"Undefined vars: " + compiler.getUndefinedVars().toString(", "));

						  })
		);
	}

	public Glasgolia() {
		this(new CompileGToR(),ClassPathResourceLoader.inst);
	}


	public CompileGToR getCompiler(){
		return compiler;
	}

	private <T> Result<T> eval(RExpr expr) {
		try {
			return Result.result((T) expr.get());
		} catch(Exception e) {
			return Result.failure(e);
		}
	}

	public Glasgolia restart() {
		Glasgolia res =  new Glasgolia(compiler.reset(),resourceLoader);
		loaded.forEach(name -> res.loadAndEval(name));
		return res;
	}

	public <T> Result<T> loadAndEval(String sourceName) {
		return compiled.apply(sourceName).flatMap(this::eval)
				.map(p -> { loaded = loaded.plus(sourceName); return (T)p; })
		;
	}

	public <T> Result<T> eval(String name, String code) {

		return compiler.compile(Source.asSource(name, code))
			.flatMap(this::eval);
	}
	*/
}
