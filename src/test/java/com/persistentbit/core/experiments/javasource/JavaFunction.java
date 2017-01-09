package com.persistentbit.core.experiments.javasource;

import com.persistentbit.core.printing.PrintTextWriter;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 9/01/17
 */
public class JavaFunction extends BaseValueClass implements PrintableText{

	private final PrintableText javaDoc;
	private final String        access;
	private final boolean       isStatic;
	private final PrintableText result;
	private final String        name;
	private final PrintableText params;
	private final PrintableText content;

	public JavaFunction(PrintableText javaDoc, String access, boolean isStatic,
						PrintableText result,
						String name,
						PrintableText params,
						PrintableText content
	) {
		this.javaDoc = javaDoc;
		this.access = access;
		this.isStatic = isStatic;
		this.result = result;
		this.name = name;
		this.params = params;
		this.content = content;
	}

	public JavaFunction doc(Object value) {
		PrintableText pt = PrintableText.from(value);
		return copyWith("javaDoc", value);
	}

	public JavaFunction asConstructor() {
		return result(empty);
	}

	public JavaFunction result(Object value) {
		return copyWith("result", PrintableText.from(value));
	}

	public JavaFunction asStatic() {
		return copyWith("isStatic", true);
	}


	public static JavaFunction name(String functionName) {
		return new JavaFunction(
			empty, "public", false, PrintableText.fromString("void"), functionName,
			PrintableText.fromString("()"),
			empty
		);
	}


	@Override
	public void print(PrintTextWriter out) {
		out.print(javaDoc);
		out.print(access);
		out.print(isStatic ? " static" : "");
		out.print(" " + result.printToString());
		out.print(" " + name);
		out.print(params);
		if(content != empty) {
			out.print(JavaBlock.of(content));
		}
		else {
			out.println(";");
		}
	}

}
