package com.persistentbit.core.javacodegen;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/05/17
 */
public enum AccessLevel{
	Public,Private, Protected, Package;

	public String label() {
		return this == Package ? "" : name().toLowerCase();
	}
}
