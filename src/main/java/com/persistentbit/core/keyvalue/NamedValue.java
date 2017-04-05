package com.persistentbit.core.keyvalue;

import com.persistentbit.core.tuples.Tuple2;

/**
 * A Named value container
 *
 * @author petermuys
 * @since 5/04/17
 * @see KeyValue
 */
public class NamedValue<VALUE> extends KeyValue<String,VALUE>{

	public NamedValue(String s, VALUE value) {
		super(s, value);
	}

	public NamedValue(Tuple2<String,VALUE> tuple){
		this(tuple._1,tuple._2);
	}

	@Override
	public NamedValue<VALUE> withValue(VALUE value) {
		return new NamedValue<>(key,value);
	}

}
