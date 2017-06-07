package com.persistentbit.core.javacodegen;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/05/17
 */
public class JMethod extends BaseValueClass{

	private final String        name;
	@Nullable
	private final String resultType;
	@Nullable
	private final PrintableText definition;
	private final boolean       isStatic;
	private final boolean       isFinal;
	private final AccessLevel accessLevel;

	@Nullable
	private final String           doc;
	private final PList<String>    annotations;
	private final PList<JArgument> arguments;
	private final PSet<JImport> imports;

	public JMethod(String name, String resultType, PrintableText definition, boolean isStatic, boolean isFinal,
				   AccessLevel accessLevel,
				   String doc,
				   PList<String> annotations,
				   PList<JArgument> arguments,
				   PSet<JImport> imports
	) {
		this.name = name;
		this.resultType = resultType;
		this.definition = definition;
		this.isStatic = isStatic;
		this.isFinal = isFinal;
		this.accessLevel = accessLevel;
		this.doc = doc;
		this.annotations = annotations;
		this.arguments = arguments;
		this.imports = imports;
	}

	public JMethod(String name, String resultType, PrintableText definition){
		this(
			name,
			resultType,
			definition,
			false,
			false,
			AccessLevel.Public,
			null,
			PList.empty(),
			PList.empty(),
			PSet.empty()

		);
	}
	public boolean isConstructor() {
		return resultType == null;
	}
	public JMethod(String name,String resultType){
		this(name,resultType,null);
	}

	public JMethod(String name){
		this(name,null);
	}

	public JMethod addArg(JArgument arg){
		return copyWith("arguments",arguments.plus(arg));
	}
	public JMethod addArg(String type, String name,boolean isNullable, String...annotations){
		return addArg(new JArgument(type,name,isNullable,PList.val(annotations),PSet.empty()));
	}
	public JMethod code(PrintableText code){
		return copyWith("definition",code);
	}

	public JMethod withAccessLevel(AccessLevel level){
		return copyWith("accessLevel",level);
	}

	public JMethod withCode(PrintableText code){
		return copyWith("definition",code);
	}

	public JMethod addAnnotation(String annotation){
		return copyWith("annotations",annotations.plus(annotation));
	}

	public JMethod asStatic() {
		return copyWith("isStatic",true);
	}

	public JMethod addImport(JImport imp){
		return copyWith("imports",imports.plus(imp));
	}
	public PSet<JImport> getAllImports(){
		return imports
			.plusAll(arguments.map(JArgument::getAllImports).flatten());
	}

	public PrintableText print() {
		return out -> {
			annotations.forEach(a -> out.println(a));
			String res = accessLevel.label();
			res = res.isEmpty()? res : res + " ";
			res = isStatic ? res + " static" : res;
			res = isFinal ? res + " final" : res;
			if(definition == null){
				res += " abstract ";
			}
			res += (resultType == null ? "" : " " + resultType + "\t") + name;
			res += "(" + arguments.toString(", ") + ")";
			if(definition == null){
				out.println(res + ";");
				return;
			}
			res += "{";
			out.println(res);
			out.indent(definition);
			out.println("}");
		};
	}
}
