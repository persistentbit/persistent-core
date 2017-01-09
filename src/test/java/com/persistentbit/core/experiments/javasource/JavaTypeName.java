package com.persistentbit.core.experiments.javasource;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 9/01/17
 */
public class JavaTypeName extends BaseValueClass{

	public final String packageName;
	public final String className;

	public JavaTypeName(String packageName, String className) {
		this.packageName = packageName;
		this.className = className;
	}

	public String toString() {
		return "TypeName[" + getFullName() + "]";
	}

	public String getFullName() {
		return packageName + "." + className;
	}

	public String getFullName(String defaultPackageName) {
		if(packageName.equals(defaultPackageName)) {
			return className;
		}
		return toString();
	}
}
