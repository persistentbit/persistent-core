package com.persistentbit.core.sourcegen;

import com.persistentbit.core.result.Result;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODOC
 * @author petermuys
 * @since 16/09/16
 */
public final class SourcePath{

  public static Result<Path> findTestSourcePath(Class<?> cls, String resourceName) {
  	return Result.function(cls.getSimpleName(),resourceName).code(l->
		findProjectPath(cls, resourceName)
			.map(p -> p.resolve("src").resolve("test").resolve("java"))
	);

  }
	public static Result<Path> findTestResourcePath(Class<?> cls, String resourceName) {
		return Result.function(cls.getSimpleName(),resourceName).code(l->
																		  findProjectPath(cls, resourceName)
																			  .map(p -> p.resolve("src").resolve("test").resolve("resources"))
		);

	}

  public static Result<Path> findProjectPath(Class<?> cls, String resourceName) {
  	return Result.function(cls.getSimpleName(),resourceName).code(l -> {
		URL url = cls.getClassLoader().getResource(resourceName);
		if(url == null) {
			Result.failure(new IllegalArgumentException("Can't find resource '" + resourceName + "' using classloader for " + cls
				.getName()));
		}
		Path f;
		try {
			f = Paths.get(url.toURI());
		} catch(URISyntaxException e) {
			return Result.failure(e);
		}
		while(f.getFileName().toString().equals("target") == false) {
			f = f.getParent();
		}
		return Result.success(f.getParent());
	});

  }

  public static Result<Path> findSourcePath(Class<?> cls, String resourceName) {
	return Result.function(cls.getSimpleName(),resourceName).code(l ->
	  findProjectPath(cls, resourceName)
		.map(p -> p.resolve("src").resolve("main").resolve("java"))
	);

  }
}
