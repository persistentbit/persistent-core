package com.persistentbit.core.keyvalue;

import com.persistentbit.core.tuples.Tuple2;

/**
 * A named String container
 *
 * @author petermuys
 * @since 5/04/17
 * @see NamedValue
 */
public class NamedString extends NamedValue<String>{

	public NamedString(String s, String s2) {
		super(s, s2);
	}

	public static NamedString of(Tuple2<String,String> tuple){
		return new NamedString(tuple._1,tuple._2);
	}
	public static NamedString of(String name, String value){
		return new NamedString(name,value);
	}

	@Override
	public NamedString withValue(String s) {
		return new NamedString(key,s);
	}
}
