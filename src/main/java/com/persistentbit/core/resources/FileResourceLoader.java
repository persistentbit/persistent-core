package com.persistentbit.core.resources;

import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.io.IORead;
import com.persistentbit.core.io.IOStreams;
import com.persistentbit.core.result.Result;

import java.io.File;
import java.util.Objects;

/**
 * TODOC
 *
 * @author petermuys
 * @since 6/02/17
 */
public class FileResourceLoader implements ResourceLoader{

	private final File rootPath;

	public FileResourceLoader(File rootPath) {
		this.rootPath = Objects.requireNonNull(rootPath);
	}

	public static FileResourceLoader forRoot(File rootPath) {
		return new FileResourceLoader(rootPath);
	}

	@Override
	public Result<PByteList> apply(String name) {
		return IOStreams.fileToInputStream(new File(rootPath, name)).flatMap(IORead::readBytes).logFunction(name);
	}



	@Override
	public String toString() {
		return "FileResourceLoader[" + rootPath.getAbsolutePath() + "]";
	}
}
