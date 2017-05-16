package com.persistentbit.core.tests.config;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 11/05/2017
 */
public class ConfigTest {
/*
    private static class Settings {
        public final Config<Integer> intTest;
        public final Config<Boolean> boolTest;
        public final Config<String> strTest;
        public final Config<RuntimeEnvironment> runEnv;
        public final Config<Integer> rangeTest;
        public final Config<PList<Integer>> intArr;
        public Settings(ConfigSource grp){
            intTest = grp.addInt("intTest", 10, "");
            boolTest = grp.addBoolean("boolTest", true, "");
            strTest = grp.addString("strTest", "Default string", "");
            runEnv = grp.addEnum("runEnv",RuntimeEnvironment.class, null,"Runtime env");
            rangeTest = grp.addInt("rangeTest",0,"Must be between 10 and 15")
            .setValidator(
                    Validator.<Integer>notNull()
                            .and(NumberValidator.minimum(10))
                            .and(NumberValidator.maximum(15))

            );
            intArr = grp.addIntArray("intArr",PList.empty(),"int array");

        }
    }


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
}
