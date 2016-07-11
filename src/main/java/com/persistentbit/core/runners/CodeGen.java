package com.persistentbit.core.runners;

import com.persistentbit.core.codegen.ImmutableCodeBuilder;

/**
 * User: petermuys
 * Date: 11/07/16
 * Time: 18:53
 */
public class CodeGen {
    static public void main(String...args){
        ImmutableCodeBuilder.build(ImmutableCodeBuilder.findSourcePath(CodeGen.class,"persistentbit.com.marker.txt"));
    }
}
