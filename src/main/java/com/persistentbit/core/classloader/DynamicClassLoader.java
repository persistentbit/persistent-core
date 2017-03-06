package com.persistentbit.core.classloader;

/**
 * TODOC
 *
 * @author petermuys
 * @since 6/03/17
 */

import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.resources.ResourceLoader;
import com.persistentbit.core.result.Result;

import java.util.HashSet;
import java.util.Set;

/**
 * Load all classes it can, leave the rest to the Parent ClassLoader.<br>
 * Based on code in https://github.com/quanla/classreloading/
 */
public class DynamicClassLoader extends ClassLoader {

	private final ResourceLoader resourceLoader;
	private final Set<String> loadedClasses = new HashSet<>();
	private final ClassLoader parent = DynamicClassLoader.class.getClassLoader();


	public DynamicClassLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (loadedClasses.contains(name)) {
			return super.loadClass(name); // Use default CL cache
		}
		Result<PByteList> loadResult = loadNewClass(name.replace('.','/') + ".class");
		if(loadResult.isPresent()){
			loadedClasses.add(name);
			return loadClass(loadResult.orElseThrow().toByteArray(),name);
		}
		Class<?> cls = parent.loadClass(name);
		loadedClasses.add(name);
		return cls;
	}

	protected Result<PByteList> loadNewClass(String name) {
		return resourceLoader.apply(name);
	}

	private Class<?> loadClass(byte[] classData, String name) {
		Class<?> cls = defineClass(name, classData, 0, classData.length);
		if (cls != null) {
			if (cls.getPackage() == null) {
				definePackage(name.replaceAll("\\.\\w+$", ""), null, null, null, null, null, null, null);
			}
			resolveClass(cls);
		}
		return cls;
	}


}