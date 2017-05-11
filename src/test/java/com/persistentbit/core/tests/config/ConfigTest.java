package com.persistentbit.core.tests.config;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.config.Config;
import com.persistentbit.core.config.ConfigGroup;
import com.persistentbit.core.io.IO;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.tests.CoreTest;
import com.persistentbit.core.utils.RuntimeEnvironment;
import com.persistentbit.core.utils.UReflect;

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
        public final Config<RuntimeEnvironment> runEnv;
        public Settings(ConfigGroup grp){
            intTest = grp.addInt("intTest", 10, "");
            boolTest = grp.addBoolean("boolTest", true, "");
            strTest = grp.addString("strTest", "Default string", "");
            runEnv = grp.addEnum("runEnv",RuntimeEnvironment.class, null,"Runtime env");
        }
    }


    static final TestCase configTest = TestCase.name("configTest").code(tr-> {
        Source src = Source.asSource(ConfigTest.class.getResource("/config/test_config.txt"), IO.utf8).orElseThrow();
        tr.info(src);
        ConfigGroup grp = new ConfigGroup();
        Settings settings = new Settings(grp);
        tr.isEquals(settings.boolTest.get().orElseThrow(),true);
        tr.isEquals(settings.intTest.get().orElseThrow(),10);
        tr.isEquals(settings.strTest.get().orElseThrow(),"Default string");
        tr.isEquals(settings.runEnv.get().orElse(null),null);
        grp.load(src).orElseThrow();
        tr.isEquals(settings.boolTest.get().orElseThrow(),false);
        tr.isEquals(settings.intTest.get().orElseThrow(),1234);
        tr.isEquals(settings.strTest.get().orElseThrow(),"Dit is een String");
        tr.isEquals(settings.runEnv.get().orElseThrow(),RuntimeEnvironment.development);
        tr.info(grp);
        tr.info(UReflect.getEnumInstances(RuntimeEnvironment.class));
    });

    public void testAll(){
        CoreTest.runTests(ConfigTest.class);
    }

    public static void main(String... args) throws Exception {
        ModuleCore.consoleLogPrint.registerAsGlobalHandler();
        new ConfigTest().testAll();
    }
}
