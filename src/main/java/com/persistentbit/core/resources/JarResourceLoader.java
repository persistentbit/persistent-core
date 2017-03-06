package com.persistentbit.core.resources;

import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.result.Result;

import java.io.File;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * TODOC
 *
 * @author petermuys
 * @since 6/03/17
 */
public class JarResourceLoader implements ResourceLoader{
	private final JarFile jarFile;

	public JarResourceLoader(JarFile jarFile) {
		this.jarFile = jarFile;
	}

	public static Result<JarResourceLoader> create(File file){
		return Result.noExceptions(() -> {
			JarFile jarFile = new JarFile(file);
			return new JarResourceLoader(jarFile);
		});

	}

	@Override
	public Result<PByteList> apply(String name) {
		ZipEntry entry = jarFile.getJarEntry(name);
		if (entry == null) {
			return Result.empty("No jar entry '" + name + "' in " + jarFile.getName());
		}
		return Result.noExceptions(()-> PByteList.from(jarFile.getInputStream(entry)));
	}
}
