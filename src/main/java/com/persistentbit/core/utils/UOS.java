package com.persistentbit.core.utils;

/**
 * TODOC
 *
 * @author petermuys
 * @since 11/03/17
 */
public class UOS{

	public static final String javaOsName = System.getProperty("os.name");

	public enum OSType{
		mac, windows, linux, unknown
	}

	public static OSType getOsType() {
		String n = javaOsName.toLowerCase().trim();
		if(n.startsWith("windows")) {
			return OSType.windows;
		}
		if(n.startsWith("osx") || n.startsWith("mac")) {
			return OSType.mac;
		}
		if(n.startsWith("linux")) {
			return OSType.linux;
		}
		return OSType.unknown;
	}

	public static boolean hasAnsiColor() {
		switch(getOsType()) {
			case windows:
				return false;
			case linux:
				return true;
			case mac:
				return true;
			default:
				return false;
		}
	}


}
