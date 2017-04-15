package com.persistentbit.core.resources;

import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.io.IORead;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.io.IO;

/**
 * TODOC
 *
 * @author petermuys
 * @since 6/02/17
 */
public class URLResourceLoader implements ResourceLoader{


	private URLResourceLoader() {
	}

	public static final URLResourceLoader inst = new URLResourceLoader();

	@Override
	public Result<PByteList> apply(String name) {
		return Result.function(name).code(l -> {
			if(name == null) {
				return Result.failure("name is null");
			}
			return IO.asURL(name)
					 .flatMap(url -> Result.noExceptions(url::openStream))
					 .flatMap(IORead::readBytes);
		});
	}

	@Override
	public String toString() {
		return "URLResourceLoader[]";
	}
}
