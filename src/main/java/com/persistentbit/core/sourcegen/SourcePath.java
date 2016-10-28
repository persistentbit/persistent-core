package com.persistentbit.core.sourcegen;

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

  public static Path findTestSourcePath(Class<?> cls, String resourceName) {
	return findProjectPath(cls, resourceName).resolve("src").resolve("test").resolve("java");
  }

  public static Path findProjectPath(Class<?> cls, String resourceName) {
	URL url = cls.getClassLoader().getResource(resourceName);
	if(url == null) {
	  throw new IllegalArgumentException("Can't find resource '" + resourceName + "' using classloader for " + cls
																												 .getName());
	}
	Path f;
	try {
	  f = Paths.get(url.toURI());
	} catch(URISyntaxException e) {
	  throw new RuntimeException(e);
	}
	while(f.getFileName().toString().equals("target") == false) {
	  f = f.getParent();
	}
	return f.getParent();
  }

  public static Path findSourcePath(Class<?> cls, String resourceName) {
	return findProjectPath(cls, resourceName).resolve("src").resolve("main").resolve("java");
  }
}
