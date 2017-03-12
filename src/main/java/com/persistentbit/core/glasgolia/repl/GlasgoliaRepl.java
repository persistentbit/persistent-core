package com.persistentbit.core.glasgolia.repl;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.glasgolia.compiler.GlasgoliaCompiler;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.UReflect;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.function.Predicate;

/**
 * TODOC
 *
 * @author petermuys
 * @since 12/03/17
 */
public class GlasgoliaRepl{

	public static final LogPrint lp = ModuleCore.consoleLogPrint;
	private ReplConfig config;

	public GlasgoliaRepl(ReplConfig config) {
		this.config = config;
	}

	public GlasgoliaRepl() {
		this(new ReplConfig());
	}

	private class Repl{

		//private Console console;
		private ClassLoader classLoader;
		private BufferedReader in;
		private PrintStream out;
		private Object compiler;
		private Class<GlasgoliaCompiler> clsCompiler;

		public Repl() {
			out = System.out;
			PList<String> excludeNames = PList.val(
				"com.persistentbit.core.resources",
				"com.persistentbit.core.glasgolia.repl",
				"com.persistentbit.core.glasgolia.gexpr"
				//"com.persistentbit.core.glasgolia.RExpr",
				//"com.persistentbit.core.result"
			);
			Predicate<String> includeNames = name -> {
				return excludeNames.find(n -> name.startsWith(n)).isPresent() == false;
			};
			//classLoader = new DynamicClassLoader(config.getClassResourceLoader(),includeNames);
			classLoader = getClass().getClassLoader();
			clsCompiler = UReflect.getClass(GlasgoliaCompiler.class.getName(), classLoader).orElseThrow();
			//replCompiler(GExprParser parser, ResourceLoader resourceLoader)
			compiler =
				UReflect.executeStaticMethod(clsCompiler, "replCompiler", config.getExprParser(), config
					.getModuleResourceLoader())
						.orElseThrow();
			System.out.println(compiler);
			RExpr res = compileCode("1+2;").orElseThrow();
			System.out.println(res);
			System.out.println(res.get());
		}

		private Result<RExpr> compileCode(String code) {
			return (Result) UReflect.executeMethod(compiler, clsCompiler, "compileCode", code);
		}
	}


	public void startRepl() {
		Repl repl = new Repl();
	}


	public static void main(String[] args) {
		lp.registerAsGlobalHandler();
		System.out.println(new File(".").getAbsoluteFile());
		new GlasgoliaRepl().startRepl();
	}
}
