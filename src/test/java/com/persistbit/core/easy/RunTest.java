package com.persistbit.core.easy;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.easyscript.EasyScript;
import com.persistentbit.core.logging.printing.LogPrintStream;

/**
 * TODOC
 *
 * @author petermuys
 * @since 27/02/17
 */
public class RunTest{

	public static void main(String[] args) {
		LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();

		EasyScript es = new EasyScript();
		Object value = es.loadAndEval("/easyscripttest.eas")
						 .orElseThrow();
		System.out.println(value);
		es.eval("speedTest", "range(1);");
	}
}
