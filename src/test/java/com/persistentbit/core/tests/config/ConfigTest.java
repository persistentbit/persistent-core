package com.persistentbit.core.tests.config;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.config.Config;
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

    private static class Settings {
        public final Config<Integer> intTest;
        public final Config<Boolean> boolTest;
        public final Config<String> strTest;
        public Settings(ConfigGroup grp){
            intTest = grp.addInt("intTest","",10);
            boolTest = grp.addBoolean("boolTest","",true);
            strTest = grp.addString("strTest","","Default string");
        }
    }


    static final TestCase configTest = TestCase.name("configTest").code(tr-> {
        Source src = Source.asSource(ConfigTest.class.getResource("/config/test_config.txt"), IO.utf8).orElseThrow();
        tr.info(src);
        ConfigGroup grp = new ConfigGroup();
        Settings settings = new Settings(grp);
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
