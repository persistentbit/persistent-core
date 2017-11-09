package com.persistentbit.core.tests.config;

import com.persistentbit.core.collections.PList;

import com.persistentbit.core.config.Config;
import com.persistentbit.core.config.ConfigGroup;
import com.persistentbit.core.config.ConfigPropertyFile;
import com.persistentbit.core.config.ConfigVar;
import com.persistentbit.core.utils.RuntimeEnvironment;
import com.persistentbit.core.validation.NumberValidator;
import com.persistentbit.core.validation.StringValidator;
import com.persistentbit.core.validation.Validator;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 11/05/2017
 */
public class ConfigTest {

    private static class Settings {
        public final ConfigVar<Integer> intTest;
        public final ConfigVar<Boolean> boolTest;
        public final ConfigVar<String> stringTest = Config.stringVar("stringTest")
            .withValidator(StringValidator.minLength(4).and(StringValidator.maxLength(255)).toValidator())
            .withInfo("Just a String config var")
            .setValue("DefaultValue");
        public Settings(ConfigGroup grp){
            intTest = Config.intVar("intTest").withInfo("Just an int").withValidator(NumberValidator.range(10,2).toValidator());
            System.out.println("int:" + intTest);
            boolTest = Config.boolVar("boolTest");
            grp  = grp.addFields(this);
            System.out.println(grp);
            //ConfigPropertyFile.load(grp,)
        }
    }
/*

    static final TestCase configTest = TestCase.name("configTest").code(tr-> {
        Source src = Source.asSource(ConfigTest.class.getResource("/config/test_config.txt"), IO.utf8).orElseThrow();
        tr.info(src);
        MemConfigSource grp = new MemConfigSource();
        Settings settings = new Settings(grp.subGroup("configtest"));
        tr.isEquals(settings.boolTest.get().orElseThrow(),true);
        tr.isEquals(settings.intTest.get().orElseThrow(),10);
        tr.isEquals(settings.strTest.get().orElseThrow(),"Default string");
        tr.isEquals(settings.runEnv.get().orElse(null),null);
        tr.isFailure(settings.rangeTest.get());
        grp.load(src).orElseThrow();
        tr.isEquals(settings.boolTest.get().orElseThrow(),false);
        tr.isEquals(settings.intTest.get().orElseThrow(),1234);
        tr.isEquals(settings.strTest.get().orElseThrow(),"Dit is een String");
        tr.isEquals(settings.runEnv.get().orElseThrow(),RuntimeEnvironment.development);
        tr.isEquals(settings.rangeTest.get().orElseThrow(),14);
        tr.info(grp);
        tr.info(UReflect.getEnumInstances(RuntimeEnvironment.class));
    });

    public void testAll(){
        CoreTest.runTests(ConfigTest.class);
    }

    public static void main(String... args) throws Exception {
        ModuleCore.consoleLogPrint.registerAsGlobalHandler();
        new ConfigTest().testAll();
    }*/

    public static void main(String[] args) {
        ConfigGroup grp = new ConfigGroup();
        new Settings(grp);
    }
}
