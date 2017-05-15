package com.persistentbit.core.experiments.mapper.test;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 13/05/17
 */
public class TypeA extends BaseValueClass{
	public final String name;
	public final TypeB valueB;
	public PList<Integer> numArr;
	public TypeA(String name, TypeB valueB,PList<Integer> numArr) {
		this.name = name;
		this.valueB = valueB;
		this.numArr = numArr;
	}
}
