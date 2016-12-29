package com.persistentbit.core.experiments;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/12/16
 */
public class Nothing{

	public static final Nothing inst = new Nothing();

	private Nothing() { }

	@Override
	public String toString() {
		return "Nothing";
	}

}
