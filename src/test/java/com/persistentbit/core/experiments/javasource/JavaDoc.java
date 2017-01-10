package com.persistentbit.core.experiments.javasource;

import com.persistentbit.core.printing.PrintTextWriter;
import com.persistentbit.core.printing.PrintableText;

/**
 * TODOC
 *
 * @author petermuys
 * @since 9/01/17
 */
public class JavaDoc implements PrintableText{

	private PrintableText content;

	private JavaDoc(PrintableText content) {
		this.content = content;
	}

	public static JavaDoc of(PrintableText pt) {
		return new JavaDoc(pt);
	}

	@Override
	public void print(PrintTextWriter out) {
		out.println("/**");
		out.print(PrintableText.indent(" * ", true, content));
		out.println(" */");
	}
}
