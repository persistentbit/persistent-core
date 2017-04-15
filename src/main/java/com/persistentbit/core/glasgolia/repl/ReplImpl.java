package com.persistentbit.core.glasgolia.repl;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.glasgolia.compiler.GlasgoliaCompiler;
import com.persistentbit.core.glasgolia.compiler.frames.ReplCompileFrame;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.glasgolia.compiler.rexpr.RJavaField;
import com.persistentbit.core.glasgolia.compiler.rexpr.RJavaMethods;
import com.persistentbit.core.glasgolia.compiler.rexpr.RLambda;
import com.persistentbit.core.glasgolia.gexpr.GExpr;
import com.persistentbit.core.io.IOFiles;
import com.persistentbit.core.io.IORead;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.parser.ParseExceptionEOF;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.io.IO;
import com.persistentbit.core.utils.UString;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * TODOC
 *
 * @author petermuys
 * @since 13/03/17
 */
public class ReplImpl implements ReplInterface{

	private static final LogPrint lp = ModuleCore.consoleLogPrint;

	private BufferedReader in;
	private PrintStream out;
	private GlasgoliaCompiler compiler;
	private GGReplCmdParser cmdParser = new GGReplCmdParser();
	private PList<String> history = PList.empty();
	private final ReplConfig config;

	public ReplImpl(ReplConfig config) {
		out = System.out;
		//out.println("config: " + config);
		this.config = config;


	}

	public String read(String existingCode, BufferedReader in) throws Exception {
		String code = existingCode;
		while(true) {
			if(code.isEmpty() == false) {
				System.out.print(".. ");
			}
			else {
				System.out.print(">> ");
			}

			System.out.flush();
			String line = in.readLine();
			if(line == null) {
				break;
			}
			code = code + line;
			break;
		}
		return code;
	}

	@Override
	public ReplAction startRepl(PList<String> execute) {
		compiler = GlasgoliaCompiler.replCompiler(config.getExprParser(), config.getModuleResourceLoader());
		config.getReplInitResourceName().ifPresent(name -> {
			if(name.endsWith(".glasg") == false){
				name = name + ".glasg";
			}
			String code = config.getModuleResourceLoader().apply(name)
			.map(pb -> pb.toText(IO.utf8)).orElseThrow();
			compileCode(code).orElseThrow().get();
		});
		execute.forEach(cmd -> {
			try {
				System.out.println(">>" + cmd);
				System.out.println(compileCode(cmd).orElseThrow().get());
			} catch(Exception e) {
				lp.print(e);
			}
		});
		while(true) {
			try {
				doRepl();
			} catch(ReloadException reload) {
				return ReplAction.reload;
				/*System.out.println("RELOADING");
				compiler = GlasgoliaCompiler.replCompiler(config.getExprParser(), config.getModuleResourceLoader());
				for(String eval : history) {
					System.out.println(">> " + eval);
					System.out.println(compileCode(eval).orElseThrow().get());
				}
				System.out.println("Done reloading");*/
			} catch(Exception e) {
				lp.print(e);
			}
		}
	}

	@Override
	public PList<String> getHistory() {
		return history;
	}


	private void doRepl() throws Exception {
		BufferedReader bin  = new BufferedReader(new InputStreamReader(System.in));
		String         code = "";
		while(true) {
			code = read(code, bin);

			if(code.startsWith(":")) {
				ParseResult<GGReplCmd> cmdResult =
					cmdParser.command(config.getExprParser()).parse(Source.asSource(code));
				if(cmdResult.isFailure()) {
					throw cmdResult.getError();
				}
				execCmd(cmdResult.getValue());

			}
			else {
				Result<Object> evalResult = compileCode(code).map(r -> r.get());
				if(evalResult.isError()) {
					Throwable error = evalResult.getEmptyOrFailureException().orElse(null);
					if(error instanceof ParseExceptionEOF && code.endsWith("\n") == false) {
						code = code + "\n";
						continue; //try next line
					}
					lp.print(evalResult.getEmptyOrFailureException().get());
				}
				else {
					history = history.plus(code);
					System.out.println("Success:" + evalResult.orElse(null));
				}
			}
			code = "";
			System.out.flush();
		}
	}

	private void execCmd(GGReplCmd cmd) {
		switch(cmd.name) {
			case "exit":
				System.exit(0);
				return;
			case "show":
				showCmd(cmd);
				return;
			case "reload":
				reloadCmd(cmd);
				return;
			case "save":
				saveCmd(cmd);
				return;
			case "load":
				loadCmd(cmd);
				return;
			case "reset":
				resetCmd(cmd);
				return;
			default:
				System.out.println("Unknown command:" + cmd.name);
		}
	}

	class ReloadException extends RuntimeException{

	}

	private void reloadCmd(GGReplCmd cmd) {
		throw new ReloadException();
	}

	private void showCmd(GGReplCmd cmd) {
		switch(cmd.params.get(0).toString()) {
			case "context":
				showContextCmd(cmd);
				return;
			case "members":
				showMembersCmd(cmd);
				return;
			default:
				throw new RuntimeException("Expected 'context' or 'members' after show");
		}

	}

	private void showContextCmd(GGReplCmd cmd) {
		ReplCompileFrame                replFrame = (ReplCompileFrame) compiler.getCompileFrame();
		PList<ReplCompileFrame.ReplVar> defs      = replFrame.getDefs();
		defs.forEach(def -> {
			System.out.println(def.nameDef.name + " = " + def.get());
		});
	}

	private void showMembersCmd(GGReplCmd cmd) {
		RExpr  expr  = compiler.compile((GExpr) cmd.params.get(1));
		Object value = expr.get();
		if(value == null) {
			System.out.println("Can't show a null value members");
		}
		Class cls = null;
		if(value instanceof RLambda) {
			RLambda lambda = (RLambda) value;
			System.out.println("Lambda " + lambda.typeDefToString());
		}
		else if(value instanceof RJavaMethods) {
			RJavaMethods jm = (RJavaMethods) value;
			for(Method m : jm.getMethods()) {
				System.out.println(m);
			}
		}
		else if(value instanceof RJavaField) {
			RJavaField javaField = (RJavaField) value;
			System.out.println(javaField.getParentValue() + "." + javaField.getField());
		}
		else if(value instanceof Class) {
			cls = (Class) value;
		}
		else {
			cls = value.getClass();
		}
		if(cls != null) {
			System.out.println("Class: " + cls.getName());
			for(Field f : cls.getFields()) {
				if(Modifier.isPublic(f.getModifiers())) {
					System.out.println("\t" + f);
				}
			}
			for(Constructor m : cls.getConstructors()) {
				if(Modifier.isPublic(m.getModifiers())) {
					System.out.println("\t" + m);
				}
			}
			for(Method m : cls.getMethods()) {
				if(Modifier.isPublic(m.getModifiers())) {
					System.out.println("\t" + m);
				}
			}
		}
	}

	private void saveCmd(GGReplCmd cmd) {
		File   f    = new File(cmd.params.getOpt(0).map(s -> s.toString()).orElse("session.glasg"));
		String code = history.fold("", (a, b) -> a + UString.NL + b);
		IOFiles.write(code, f, IO.utf8);
		System.out.println("Session saved to " + f.getAbsolutePath());
	}

	private void loadCmd(GGReplCmd cmd) {
		File f = new File(cmd.params.getOpt(0).map(s -> s.toString()).orElse("session.glasg"));
		String res = IORead.readTextFile(f, IO.utf8)
						   .ifPresent(s -> history = PList.val(s.getValue().trim()))
						   .ifPresent(s -> System.out.println("loaded " + f.getAbsolutePath()))
						   .orElseThrow().trim();
		throw new ReloadException();
	}

	public void resetCmd(GGReplCmd cmd) {
		history = PList.empty();
		throw new ReloadException();
	}


	/*public GGRepl loadAndEval(String sourceName) {
		gg.loadAndEval(sourceName).throwOnError().orElse(null);
		return this;
	}*/


	private Result<RExpr> compileCode(String code) {
		//return (Result) UReflect.executeMethod(compiler, clsCompiler, "compileCode", code);
		return compiler.compileCode(code);
	}
}
