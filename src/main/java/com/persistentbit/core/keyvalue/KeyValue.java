package com.persistentbit.core.keyvalue;

import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * A Key and a Value Container
 *
 * @author petermuys
 * @since 5/04/17
 * @see NamedString
 */
public class KeyValue<KEY,VALUE> extends BaseValueClass{
	public final KEY key;
	public final VALUE value;

	public KeyValue(KEY key, VALUE value) {
		this.key = key;
		this.value = value;
	}


	public KeyValue<KEY,VALUE> withValue(VALUE value){
		return new KeyValue<>(key,value);
	}

	@Override
	public String toString() {
		return "(" + key + " -> " + value + ")";
	}
	public Tuple2<KEY,VALUE> tuple(){
		return Tuple2.of(key,value);
	}
}
