package com.persistentbit.core.experiments.javasource;

import com.persistentbit.core.printing.PrintTextWriter;
import com.persistentbit.core.printing.PrintableText;

/**
 * TODOC
 *
 * @author petermuys
 * @since 9/01/17
 */
public class JavaBlock implements PrintableText{

	private final PrintableText content;

	public JavaBlock(PrintableText content) {
		this.content = content;
	}

	static public JavaBlock of(PrintableText content) {
		return new JavaBlock(content);
	}

	@Override
	public void print(PrintTextWriter out) {
		out.println("{");
		out.indent(content);
		out.println("}");
	}
}
