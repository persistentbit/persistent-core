package com.persistentbit.core.experiments.config;

import com.persistentbit.core.io.IO;
import com.persistentbit.core.io.IOStreams;
import com.persistentbit.core.result.Result;

import java.io.File;
import java.util.Properties;

public class BravoConfigService {

    static public class ConfigDocGen{
        public final ConfigPath docOutputDirectory = new ConfigPath("docOutputDirectory")
                .setInfo("Documenten output path");

        public final ConfigPath listOutputDirectory = new ConfigPath("listOutputDirectory")
                .setInfo("Lijsten output path");

    }
    public final ConfigBool eidEnabled = new ConfigBool("cp.eid.enabled").set(Result.success(false));
    public final ConfigBool digilokEnabled = new ConfigBool("bevolking.digilok.enabled").set(Result.success(false));
    public final ConfigString nisCodeGemeente = new ConfigString("nisCodeGemeente");
    public final ConfigDocGen docGen = new ConfigDocGen();



    public static void main(String... args) throws Exception {

        BravoConfigService service = new BravoConfigService();
        System.out.println(service.nisCodeGemeente.get());
        Properties properies = new Properties();
        properies.load(IOStreams.fileToReader(new File("D:\\bravoconfig\\conf\\bevolking-server.config.properties"), IO.utf8).orElseThrow());
        properies.list(System.out);
    }
}
