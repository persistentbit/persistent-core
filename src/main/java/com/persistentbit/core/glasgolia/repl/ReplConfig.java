package com.persistentbit.core.glasgolia.repl;

import com.persistentbit.core.glasgolia.gexpr.GExprParser;
import com.persistentbit.core.resources.ResourceLoader;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Config settings for a Glasgolia Repl
 *
 * @author petermuys
 * @see GlasgoliaRepl
 * @since 12/03/17
 */
public class ReplConfig extends BaseValueClass{

	private final ResourceLoader classResourceLoader;
	private final ResourceLoader moduleResourceLoader;
	private final GExprParser exprParser;

	public ReplConfig(ResourceLoader classResourceLoader,
					  ResourceLoader moduleResourceLoader,
					  GExprParser exprParser
	) {
		this.classResourceLoader = classResourceLoader;
		this.moduleResourceLoader = moduleResourceLoader;
		this.exprParser = exprParser;
	}

	public ReplConfig() {
		this(
			ResourceLoader.rootAndClassPath,    //classes
			ResourceLoader.rootAndClassPath,    //modules
			new GExprParser()
		);
	}

	public ReplConfig withClassResourceLoader(ResourceLoader classResourceLoader) {
		return copyWith("classResourceLoader", classResourceLoader);
	}

	public ReplConfig withModuleResourceLoader(ResourceLoader moduleResourceLoader) {
		return copyWith("moduleResourceLoader", moduleResourceLoader);
	}

	public ReplConfig withExprParser(GExprParser exprParser) {
		return copyWith("exprParser", exprParser);
	}

	public ResourceLoader getClassResourceLoader() {
		return classResourceLoader;
	}

	public ResourceLoader getModuleResourceLoader() {
		return moduleResourceLoader;
	}

	public GExprParser getExprParser() {
		return exprParser;
	}
}
