package com.persistentbit.core.tests.config;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.config.ConfigGroup;
import com.persistentbit.core.io.IO;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.tests.CoreTest;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 11/05/2017
 */
public class ConfigTest {

    static final TestCase configTest = TestCase.name("configTest").code(tr-> {
        Source src = Source.asSource(ConfigTest.class.getResource("/config/test_config.txt"), IO.utf8).orElseThrow();
        tr.info(src);
        ConfigGroup grp = new ConfigGroup();
        ConfigGroup.Property<Integer> intTest = grp.add("intTest",Integer.class,"int value test", -1);
        ConfigGroup.Property<Boolean> boolTest = grp.add("boolTest",Boolean.class,"bool value test", true);
        ConfigGroup.Property<String> strTest = grp.add("strTest",String.class,"string value test", "This is the default value");
        grp.load(src).orElseThrow();

    });

    public void testAll(){
        CoreTest.runTests(ConfigTest.class);
    }

    public static void main(String... args) throws Exception {
        ModuleCore.consoleLogPrint.registerAsGlobalHandler();
        new ConfigTest().testAll();
    }
}
