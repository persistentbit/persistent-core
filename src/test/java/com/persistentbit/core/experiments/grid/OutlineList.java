package com.persistentbit.core.experiments.grid;

import com.persistentbit.core.collections.PList;

/**
 * TODOC
 *
 * @author petermuys
 * @since 24/03/17
 */
public class OutlineList implements OutlineDoc{
	private final PList<OutlineDoc> elements;

	public OutlineList(
		PList<OutlineDoc> elements
	) {
		this.elements = elements;
	}

	public OutlineList(OutlineDoc...elements){
		this(PList.val(elements));
	}

	public PList<OutlineDoc> getElements() {
		return elements;
	}
	public OutlineDoc	withElement(int index, OutlineDoc element){
		return new OutlineList(elements.put(index,element));
	}
}
