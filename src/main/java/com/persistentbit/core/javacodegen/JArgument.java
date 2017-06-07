package com.persistentbit.core.javacodegen;

import com.persistentbit.core.collections.PList;
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

	public JArgument(String type, String name, boolean isNullable,PList<String> annotations) {
		this.type = type;
		this.name = name;
		this.isNullable = isNullable;
		this.annotations = annotations;
	}
	public JArgument(String type, String name){
		this(type,name,false, PList.empty());
	}
	public JArgument asNullable(){
		return copyWith("isNullable",true);
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
}
