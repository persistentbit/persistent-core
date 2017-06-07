package com.persistentbit.core.javacodegen;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/05/17
 */
public class JArgument extends BaseValueClass{
	private final String type;
	private final String name;
	private boolean isNullable;
	private final PList<String> annotations;
	private final PSet<JImport> imports;

	public JArgument(String type, String name, boolean isNullable,PList<String> annotations,PSet<JImport> imports) {
		this.type = type;
		this.name = name;
		this.isNullable = isNullable;
		this.annotations = annotations;
		this.imports = imports;
	}
	public JArgument(String type, String name){
		this(type,name,false, PList.empty(),PSet.empty());
	}
	public JArgument asNullable(){
		return addImport(JImport.forClass(Nullable.class)).copyWith("isNullable",true);
	}
	public boolean isNullable(){
		return isNullable;
	}
	public String toString() {
		String annStr = annotations.toString(" ");
		if(isNullable){
			annStr = "@Nullable" + " " + annStr;
		}
		annStr = annStr.trim().isEmpty() ? "" : annStr.trim() + " ";
		return  annStr + type + " " + name;
	}

	public JArgument	addImport(JImport imp){
		return copyWith("imports",imports.plus(imp));
	}

	public PSet<JImport> getAllImports(){
		return imports;
	}
}
