package com.persistentbit.core.glasgolia;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.parser.ParseExceptionEOF;
import com.persistentbit.core.result.Result;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * TODOC
 *
 * @author petermuys
 * @since 21/02/17
 */
public class ERepl{

	public static final LogPrint lp =
		LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();

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
			if(true) break;
			//if(line.trim().isEmpty()) {
			//	break;
			//}
			//code += UString.NL + line;
		}
		return code;
	}


	public void repl() throws Exception {
		BufferedReader bin  = new BufferedReader(new InputStreamReader(System.in));
		String         code = "";
		while(true) {
			code = read(code, bin);
			if(code.trim().equals(":exit")) {
				break;
			}
			Result<Object> evalResult = gg.eval("repl", code);
			if(evalResult.isError()) {
				Throwable error = evalResult.getEmptyOrFailureException().orElse(null);
				if(error instanceof ParseExceptionEOF && code.endsWith("\n") == false) {
					code = code + "\n";
					continue; //try next line
				}
				lp.print(evalResult.getEmptyOrFailureException().get());
			}
			else {

				System.out.println("Success:" + evalResult.orElse(null));
			}
			code = "";
			System.out.flush();
		}
	}

	public final Glasgolia gg = new Glasgolia();

	public ERepl loadAndEval(String sourceName) {
		gg.loadAndEval(sourceName).throwOnError().orElse(null);
		return this;
	}


	public ERepl() {
		loadAndEval("repl.gg");
	}

	public static void main(String[] args) throws Exception {
		ERepl repl = new ERepl();


		repl.repl();


	}
}
