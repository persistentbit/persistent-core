package com.persistentbit.core.experiments.config;

import com.persistentbit.core.io.IO;
import com.persistentbit.core.io.IOStreams;
import com.persistentbit.core.result.Result;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * TODOC
 *
 * @author petermuys
 * @since 15/05/17
 */
public class ConfigPropertyFile{

	static Result<ConfigGroup> load(ConfigGroup grp, File file){
		return IOStreams.fileToReader(file,IO.utf8)
			.flatMap(reader -> load(grp, reader))
			.logFunction(grp,file);
	}

	static Result<ConfigGroup> load(ConfigGroup grp, InputStream in){
		return IOStreams.inputStreamToReader(in,IO.utf8)
			.flatMap(reader -> load(grp,reader))
			.logFunction(grp,in);
	}
	static Result<ConfigGroup> load(ConfigGroup grp, Reader reader){
		return Result.function(grp,reader).code(l -> {
			if(grp == null){
				return Result.failure("grp is null");
			}
			if(reader == null){
				return Result.failure("reader is null");
			}
			return Result.noExceptions(() -> {
				Properties props = new Properties();
				props.load(reader);
				return props;
			}).flatMap(props -> {

				for(String name : props.stringPropertyNames()){
					ConfigVar var = grp.getVar(name).orElse(null);
					if(var == null){
						continue;
					}
					Result validateResult = var.validateStringValue(props.getProperty(name));
					if(validateResult.isError()){
						return validateResult.map(v -> null);
					}
				}
				for(String name : props.stringPropertyNames()) {
					ConfigVar var = grp.getVar(name).orElse(null);
					if(var == null) {
						continue;
					}
					var.setString(props.getProperty(name));
				}
				return Result.success(grp);
			});
		});
	}

	public static void main(String[] args) {
		load(new ConfigGroup(),ConfigPropertyFile.class.getResourceAsStream("/config/test.properties")).orElseThrow();
	}
}
