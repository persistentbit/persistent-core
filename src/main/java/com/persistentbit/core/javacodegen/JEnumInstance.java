package com.persistentbit.core.javacodegen;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 11/06/17
 */
public class JEnumInstance extends BaseValueClass{
	private final String name;
	private final PList<String>	arguments;

	public JEnumInstance(String name, PList<String> arguments) {
		this.name = name;
		this.arguments = arguments;
		checkNullFields();
	}

	public JEnumInstance(String name){
		this(name,PList.empty());
	}
	public JEnumInstance addArgument(String value){
		return copyWith("arguments",arguments.plus(value));
	}
	public PrintableText	print(){
		return out -> {
			String res = name;
			if(arguments.isEmpty() == false){
				res = res + "(" + arguments.toString(", ") + ")";
			}
			out.print(res);
		};
	}
}
