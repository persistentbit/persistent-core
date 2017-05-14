package com.persistentbit.core.experiments.mapper.test;

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

	public TypeADTO(String name, String typeBName) {
		this.name = name;
		this.typeBName = typeBName;
	}
}
