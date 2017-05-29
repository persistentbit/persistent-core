package com.persistentbit.core.javacodegen;

import com.persistentbit.core.collections.PList;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/05/17
 */
public class JArgument{
	private final String type;
	private final String name;
	private final PList<String> annotations;

	public JArgument(String type, String name, PList<String> annotations) {
		this.type = type;
		this.name = name;
		this.annotations = annotations;
	}
	public JArgument(String type, String name){
		this(type,name,PList.empty());
	}
	public String toString() {
		return annotations.toString(" ") + type + " " + name;
	}
}
