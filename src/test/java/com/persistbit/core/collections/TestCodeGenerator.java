package com.persistbit.core.collections;

import com.persistentbit.core.codegen.CaseClassCodeBuilder;
import com.persistentbit.core.tuples.Tuple2;
import org.testng.annotations.Test;

import java.io.File;

/**
 *@author petermuys
 *@since 11/07/16
 */
public class TestCodeGenerator {



    @Test
    public void testCodeGenerator() {
        Tuple2 tuple2 = null;

        File source = CaseClassCodeBuilder.findSourcePath(Tuple2.class, "test.persistentbit.com.marker.txt");
        File testSource = new File(source,"../../test/java");
        CaseClassCodeBuilder.build(testSource, TestCodeGenerator.class.getClassLoader());
    }
}
