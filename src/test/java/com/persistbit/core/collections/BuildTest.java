package com.persistbit.core.collections;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.Immutable;
import com.persistentbit.core.properties.FieldNames;

import java.util.Optional;

/**
 * User: petermuys
 * Date: 11/07/16
 * Time: 18:01
 */
@Immutable
public class BuildTest<A> {
    private final String first;
    @Nullable
    private final String middle;
    private final String last;
    private final A a;

    @FieldNames(names = {"first", "middle", "last", "a"})
    public BuildTest(String first, String middle, String last, A a) {
        this.first = first;
        this.middle = middle;
        this.last = last;
        this.a = a;
    }



	//Generated by com.persistentbit.core.codegen.ImmutableCodeBuilder

	public BuildTest<A>	 withFirst(String value){
		return new BuildTest<>(value, this.middle, this.last, this.a);
	} 

	public String getFirst(){ return first; }

	public BuildTest<A>	 withMiddle(String value){
		return new BuildTest<>(this.first, value, this.last, this.a);
	} 

	public Optional<String> getMiddle(){ return Optional.ofNullable(middle); }

	public BuildTest<A>	 withLast(String value){
		return new BuildTest<>(this.first, this.middle, value, this.a);
	} 

	public String getLast(){ return last; }

	public BuildTest<A>	 withA(A value){
		return new BuildTest<>(this.first, this.middle, this.last, value);
	} 

	public A getA(){ return a; }

}