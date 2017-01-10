package com.persistentbit.core.experiments.javasource;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.printing.PrintTextWriter;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 9/01/17
 */
public class JavaClass extends BaseValueClass implements PrintableText{

	static private class JavaTypeDef extends BaseValueClass{

		public final PrintableText doc;
		public final PrintableText access;
		public final boolean       isFinal;

		public final PrintableText extendNames;
		public final PrintableText implementNames;

		public JavaTypeDef(PrintableText doc, PrintableText access, boolean isFinal,
						   PrintableText extendNames,
						   PrintableText implementNames
		) {
			this.doc = doc;
			this.access = access;
			this.isFinal = isFinal;
			this.extendNames = extendNames;
			this.implementNames = implementNames;
		}

		public JavaTypeDef() {
			this(empty, PrintableText.fromString("public"), true, empty, empty);
		}

		public JavaTypeDef javaDoc(Object docContent) {
			return doc(JavaDoc.of(PrintableText.from(docContent)));
		}

		public JavaTypeDef doc(Object docContent) {
			return copyWith("doc", PrintableText.from(docContent));
		}
	}

	private final PSet<JavaTypeName>   imports;
	private final JavaTypeName         typeName;
	private final JavaTypeDef          typeDef;
	private final PList<PrintableText> fields;
	private final PList<PrintableText> constructors;
	private final PList<PrintableText> methods;


	public JavaClass(
		PSet<JavaTypeName> imports,
		JavaTypeName typeName,
		JavaTypeDef typeDef,
		PList<PrintableText> fields,
		PList<PrintableText> constructors,
		PList<PrintableText> methods
	) {
		this.imports = imports;
		this.typeName = typeName;
		this.typeDef = typeDef;
		this.fields = fields;
		this.constructors = constructors;
		this.methods = methods;
	}

	public static JavaClass of(JavaTypeName typeName) {
		return new JavaClass(
			PSet.empty(),
			typeName,
			new JavaTypeDef(),
			PList.empty(),
			PList.empty(),
			PList.empty()
		);
	}

	public static JavaClass of(Object packageName, Object className) {
		return of(new JavaTypeName(PrintableText.from(packageName).printToString(), PrintableText.from(className)
			.printToString()));
	}


	public JavaClass javaDoc(Object docContent) {
		return copyWith("typeDef", typeDef.javaDoc(PrintableText.from(docContent)));
	}

	public JavaClass doc(Object docContent) {
		return copyWith("typeDef", typeDef.doc(PrintableText.from(docContent)));
	}

	public JavaClass addMethod(PrintableText method) {
		return copyWith("methods", methods.plus(method));
	}

	public JavaClass addField(Object field){
		return copyWith("fields", fields.plus(PrintableText.from(field)));
	}

	@Override
	public void print(PrintTextWriter out) {
		out.println("package " + typeName.packageName + ";");
		out.println();
		out.print(this.typeDef.doc);
		out.print(this::printTypeDef);
		out.print(JavaBlock.of(this::printContent));

	}

	private JavaTypeName of(String name) {
		int i = name.lastIndexOf('.');
		if(i < 0) {
			//Name without a package -> assume same package as class
			return new JavaTypeName(typeName.packageName, name);
		}
		else {
			return new JavaTypeName(name.substring(0, i), name.substring(1 + 1));
		}
	}

	private void printContent(PrintTextWriter out) {
		out.println(PrintableText.indent(cout -> {
			cout.println();
			fields.forEach(f -> {
				cout.println(f);

			});
			cout.println();
			methods.forEach(m -> {
				cout.println(m);
				cout.println();
			});
		}));

	}

	private void printTypeDef(PrintTextWriter out) {
		out.print(typeDef.access);
		if(typeDef.isFinal) {
			out.print(" final");
		}
		out.print(" class ");
		out.print(typeName.className);
		if(typeDef.extendNames != empty) {
			out.print(" extends" + typeDef.extendNames.printToString());
		}
		if(typeDef.implementNames != empty) {
			out.print(" implements " + typeDef.implementNames.printToString());
		}

	}
}
