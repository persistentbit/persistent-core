package com.persistentbit.core.glasgolia.repl;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.glasgolia.compiler.GlasgoliaCompiler;
import com.persistentbit.core.glasgolia.compiler.frames.ReplCompileFrame;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.parser.ParseExceptionEOF;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.IO;
import com.persistentbit.core.utils.UString;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;

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
		out.println("config: " + config);
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
	public void startRepl() {
		compiler = GlasgoliaCompiler.replCompiler(config.getExprParser(), config.getModuleResourceLoader());
		while(true) {
			try {
				doRepl();
			} catch(ReloadException reload) {
				System.out.println("RELOADING");
				compiler = GlasgoliaCompiler.replCompiler(config.getExprParser(), config.getModuleResourceLoader());
				for(String eval : history) {
					System.out.println(">> " + eval);
					System.out.println(compileCode(eval).orElseThrow().get());
				}
				System.out.println("Done reloading");
			} catch(Exception e) {
				lp.print(e);
			}
		}

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
		ReplCompileFrame                replFrame = (ReplCompileFrame) compiler.getCompileFrame();
		PList<ReplCompileFrame.ReplVar> defs      = replFrame.getDefs();
		defs.forEach(def -> {
			System.out.println(def.nameDef.name + " = " + def.get());
		});
		/*CompileGToR                  compiler = gg.getCompiler();
		CompileContext               ctx      = compiler.getContext();
		PList<CompileContext.ValVar> all      = ctx.getCurrentFrame().getAllValVars().plist();
		all.forEach(vv -> {
			System.out.println(vv.show() + " = " + gg.eval("repl.show",vv.name).orElse("?"));
		});*/
	}

	private void saveCmd(GGReplCmd cmd) {
		File   f    = new File(cmd.params.getOpt(0).map(s -> s.toString()).orElse("session.gg"));
		String code = history.fold("", (a, b) -> a + UString.NL + b);
		IO.writeFile(code, f, IO.utf8);
		System.out.println("Session saved to " + f.getAbsolutePath());
	}

	private void loadCmd(GGReplCmd cmd) {
		File f = new File(cmd.params.getOpt(0).map(s -> s.toString()).orElse("session.gg"));
		String res = IO.readTextFile(f, IO.utf8)
					   .ifPresent(s -> history = PList.val(s.getValue().trim()))
					   .ifPresent(s -> System.out.println("loaded " + f.getAbsolutePath()))
					   .orElseThrow();
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
