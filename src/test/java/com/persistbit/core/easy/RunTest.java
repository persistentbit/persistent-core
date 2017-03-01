package com.persistbit.core.easy;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.easyscript.EasyScript;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.result.Result;

/**
 * TODOC
 *
 * @author petermuys
 * @since 27/02/17
 */
public class RunTest{

	public static void main(String[] args) {
		LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();

		EasyScript     es       = new EasyScript();
		Result<Object> resValue = es.loadAndEval("/easyscripttest.eas");
		if(resValue.isError()) {
			resValue.orElseThrow();
		}
		System.out.println(resValue.orElse(null));
		es.eval("speedTest", "range(100);");
	}
}
