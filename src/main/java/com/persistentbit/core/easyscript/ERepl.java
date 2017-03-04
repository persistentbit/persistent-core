package com.persistentbit.core.easyscript;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.UString;

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

			if(line.trim().isEmpty()) {
				break;
			}
			code += UString.NL + line;
		}
		return code;
	}

	static final LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();

	/*static EEvalResult eval(EvalContext context, String code) {
		ParseResult<EExpr> pr = EParser.ws.skipAnd(EParser.parseExprList()).skip(Scan.eof).parse(Source.asSource(code));
		if(pr.isSuccess()) {
			System.out.println("Parsed: " + pr.getValue());
			return EEvaluator.eval(context, pr.getValue());
		}
		lp.print(pr.getError());
		return null;
	}*/

	static final EasyScript es = new EasyScript();

	public static void main(String[] args) throws Exception {
		EvalContext    context = EvalContext.inst;
		/*context = context
			.addImport("java.lang")
			.addImport("java.util")
			.addImport("com.persistentbit.core.collections")
			.addImport("com.persistentbit.core.result")
			.addImport("com.persistentbit.core.utils")
			.addImport("com.persistentbit.core");

		;*/

		es.loadAndEval("repl.easy").orElseThrow();

		/*String defaultCode = IO.getClassPathResourceReader("/repl.easy",IO.utf8)
				.flatMap(IO::readTextStream)
				.orElse(null);
		if(defaultCode!= null){
			EEvalResult res = es.eval(context,defaultCode);
			if(res != null) {
				if (res.isSuccess()) {

					context = res.getContext();
					System.out.println("Success:" + res.getValue());

				} else {
					lp.print(res.getError());
				}
			}
			System.out.flush();
		}
*/
		BufferedReader bin     = new BufferedReader(new InputStreamReader(System.in));
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
}
