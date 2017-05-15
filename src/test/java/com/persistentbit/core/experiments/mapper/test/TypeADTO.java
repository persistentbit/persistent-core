package com.persistentbit.core.experiments.mapper.test;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 13/05/17
 */
public class TypeADTO extends BaseValueClass{
	public final String name;
	public final String typeBName;
	public final PList<String> array;
	public TypeADTO(String name, String typeBName,PList<String> array) {
		this.name = name;
		this.typeBName = typeBName;
		this.array = array;
	}
}
