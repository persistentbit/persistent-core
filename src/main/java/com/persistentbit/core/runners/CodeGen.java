package com.persistentbit.core.runners;

import com.persistentbit.core.codegen.CaseClaseCodeBuilder;

/**
 * User: petermuys
 * Date: 11/07/16
 * Time: 18:53
 */
public class CodeGen {
    static public void main(String...args){
        CaseClaseCodeBuilder.build(CaseClaseCodeBuilder.findSourcePath(CodeGen.class,"persistentbit.com.marker.txt"));
    }
}
