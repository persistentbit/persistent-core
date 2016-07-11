package com.persistbit.core.collections;

import com.persistentbit.core.Tuple2;
import com.persistentbit.core.codegen.ImmutableCodeBuilder;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by petermuys on 11/07/16.
 */
public class TestCodeGenerator {



    @Test
    public void testCodeGenerator() {
        Tuple2 tuple2 = null;

        File source = ImmutableCodeBuilder.findSourcePath(Tuple2.class,"test.persistentbit.com.marker.txt");
        File testSource = new File(source,"../../test/java");
        ImmutableCodeBuilder.build(testSource,TestCodeGenerator.class.getClassLoader());
    }
}
