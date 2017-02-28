package com.persistentbit.core.experiments;

import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * TODOC
 *
 * @author petermuys
 * @since 27/02/17
 */
public class MethodHandling{

	public static String get(String name) {
		return "Hello " + name;
	}

	public static MethodHandles.Lookup lookup = MethodHandles.lookup();

	public static void main(String[] args) throws Throwable {
		MethodType   type   = MethodType.methodType(PrintStream.class);
		MethodHandle handle = lookup.findStaticGetter(System.class, "out", PrintStream.class);
		Object       result = handle.invoke();
		System.out.println(result);

		MethodType   type2   = MethodType.methodType(String.class);
		MethodHandle handle2 = lookup.findStatic(MethodHandling.class, "get", type2);
		Object       result2 = handle2.invoke();
		System.out.println(result2);
	}
}
