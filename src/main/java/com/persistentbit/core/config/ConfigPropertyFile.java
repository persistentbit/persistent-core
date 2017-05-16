package com.persistentbit.core.config;

import com.persistentbit.core.io.IO;
import com.persistentbit.core.io.IOStreams;
import com.persistentbit.core.result.Result;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.*;
import java.util.List;
import java.util.Properties;

/**
 * TODOC
 *
 * @author petermuys
 * @since 15/05/17
 */
public class ConfigPropertyFile{

	public static Result<ConfigGroup> load(ConfigGroup grp, File file){
		return IOStreams.fileToReader(file,IO.utf8)
			.flatMap(reader -> load(grp, reader))
			.logFunction(grp,file);
	}

	public static Result<ConfigGroup> load(ConfigGroup grp, InputStream in){
		return IOStreams.inputStreamToReader(in,IO.utf8)
			.flatMap(reader -> load(grp,reader))
			.logFunction(grp,in);
	}
	public static Result<ConfigGroup> load(ConfigGroup grp, Reader reader){
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
	static public WatchService watchFileForChange(ConfigGroup grp, Path propertyFilePath){

	}
	static public void watchForChange(Path path){
		//define a folder root
		Path myDir = Files.exists(path) && Files.isRegularFile(path)
				? path.getParent()
				: path;

		try {
			WatchService watcher = myDir.getFileSystem().newWatchService();
			myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

			WatchKey watckKey = watcher.take();
			List<WatchEvent<?>> events = watckKey.pollEvents();
			for (WatchEvent event : events) {
				if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
					System.out.println("Created: " + event.context().toString());
				}
				if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
					System.out.println("Delete: " + event.context().toString());
				}
				if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
					System.out.println("Modify: " + event.context().toString());
				}
			}

		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
		}

	}

	public static void main(String[] args) {
		//load(new ConfigGroup(),ConfigPropertyFile.class.getResourceAsStream("/config/test.properties")).orElseThrow();
		Path p = Paths.get("D:\\bravoconfig\\conf\\bevolking-server.config.properties");
		watchForChange(p);
	}
}
