package com.persistentbit.core.glasgolia.repl;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.classloader.DynamicClassLoader;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.utils.UReflect;

import java.io.File;
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
	private ClassLoader classLoader;

	public GlasgoliaRepl(ReplConfig config) {
		this.config = config;
	}

	public GlasgoliaRepl() {
		this(new ReplConfig());
	}


	public void startRepl() {
		PList<String> excludeNames = PList.val(
			"com.persistentbit.core.resources",
			"com.persistentbit.core.glasgolia.repl.ReplConfig",
			"com.persistentbit.core.glasgolia.repl.ReplInterface",
			"com.persistentbit.core.glasgolia.gexpr",
			"com.persistentbit.core.parser",
			"com.persistentbit.core.collections",
			"com.persistentbit.core.glasgolia.ETypeSig",
			"com.persistentbit.core.utils.StrPos",
			"com.persistentbit.core.tuples",
			"com.persistentbit.core.result"
		);

		Predicate<String> includeNames = name -> {
			return excludeNames.find(n -> name.startsWith(n)).isPresent() == false;
		};
		classLoader = new DynamicClassLoader(config.getClassResourceLoader(), includeNames);
		//classLoader = getClass().getClassLoader();
		Class         clsRepl =
			UReflect.getClass("com.persistentbit.core.glasgolia.repl.ReplImpl", classLoader).orElseThrow();
		ReplInterface repl    = (ReplInterface) UReflect.executeConstructor(clsRepl, config).orElseThrow();
		System.out.println(repl);
		repl.startRepl();
	}


	public static void main(String[] args) {
		lp.registerAsGlobalHandler();
		System.out.println(new File(".").getAbsoluteFile());
		new GlasgoliaRepl().startRepl();
	}
}
