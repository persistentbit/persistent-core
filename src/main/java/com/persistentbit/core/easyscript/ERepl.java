package com.persistentbit.core.easyscript;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.Scan;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.utils.StringUtils;

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
			code += StringUtils.NL + line;
		}
		return code;
	}

	static final LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();

	static EEvalResult eval(EvalContext context, String code) {
		ParseResult<EExpr> pr = EParser.ws.skipAnd(EParser.parseExprList()).skip(Scan.eof).parse(Source.asSource(code));
		if(pr.isSuccess()) {
			System.out.println("Parsed: " + pr.getValue());
			return EEvaluator.eval(context, pr.getValue());
		}
		lp.print(pr.getError());
		return null;
	}

	public static void main(String[] args) throws Exception {
		EvalContext    context = EvalContext.inst;
		BufferedReader bin     = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			String code = read(bin);
			if(code.trim().equals(":exit")) {
				break;
			}
			EEvalResult evalResult = eval(context, code);
			if(evalResult != null) {
				if (evalResult.isSuccess()) {

					context = evalResult.getContext();
					System.out.println("Success:" + evalResult.getValue());

				} else {
					lp.print(evalResult.getError());
				}
			}
			System.out.flush();
		}


	}
}
