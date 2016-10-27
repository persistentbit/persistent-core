package com.persistentbit.core.runners;

import com.persistentbit.core.codegen.CaseClassCodeBuilder;

/**
 * User: petermuys
 * Date: 11/07/16
 * Time: 18:53
 */
public final class CodeGen {
    public static void main(String...args){
        CaseClassCodeBuilder.build(CaseClassCodeBuilder.findSourcePath(CodeGen.class, "persistentbit.com.marker.txt"));
    }
}
