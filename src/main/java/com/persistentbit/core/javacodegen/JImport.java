package com.persistentbit.core.javacodegen;

import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/05/17
 */
public class JImport extends BaseValueClass{
	private final String name;
	private final boolean isStatic;

	public JImport(String name, boolean isStatic) {
		this.name = name;
		this.isStatic = isStatic;
	}
	public JImport(String name){
		this(name,false);
	}
	public JImport(Class cls){
		this(cls.getName());
	}
	public JImport asStatic() {
		return copyWith("isStatic",true);
	}
	public PrintableText print() {
		return out -> {
			String res = "import ";
			res += isStatic ? " static " : "";
			res += name;
			res += ";";
			out.println(res);
		};
	}

	public boolean includeForPackage(String packageName){
		if(isStatic){
			return true;
		}
		return name.startsWith(packageName) == false;
	}
}
