package com.persistentbit.core.glasgolia;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
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


	static String read(BufferedReader in) throws Exception {
		String code = "";
		while(true) {
			System.out.print(">> ");
			System.out.flush();
			String line = in.readLine();
			if(line == null) {
				break;
			}
			code = line;
			if(true) break;
			//if(line.trim().isEmpty()) {
			//	break;
			//}
			//code += UString.NL + line;
		}
		return code;
	}

	static final LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();


	static public void repl() throws Exception {
		BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			String code = read(bin);
			if(code.trim().equals(":exit")) {
				break;
			}
			Result<Object> evalResult = es.eval("repl", code);
			if(evalResult.isError()) {
				lp.print(evalResult.getEmptyOrFailureException().get());
			}
			else {
				System.out.println("Success:" + evalResult.orElse(null));
			}
			System.out.flush();
		}
	}

	static public final Glasgolia es = new Glasgolia();

	public static void main(String[] args) throws Exception {
		es.loadAndEval("repl.gg").throwOnError().orElse(null);
		repl();


	}
}
