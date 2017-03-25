package com.persistentbit.core.experiments.grid;

/**
 * TODOC
 *
 * @author petermuys
 * @since 24/03/17
 */
public class OutlineText implements OutlineDoc{
	private final String text;

	public OutlineText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
