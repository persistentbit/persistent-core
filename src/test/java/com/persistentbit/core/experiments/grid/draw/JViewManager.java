package com.persistentbit.core.experiments.grid.draw;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.experiments.grid.View;
import com.persistentbit.core.experiments.grid.ViewManager;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 24/03/17
 */
public class JViewManager<DOC> implements ViewManager<DOC>{

	private DOC doc;
	private PList<View<DOC>> views	=	PList.empty();

	public JViewManager() {

	}
	public void setDoc(DOC doc){
		this.doc = doc;
		views.forEach(v -> v.docUpdated(doc));
	}


	public void addView(View<DOC> view){
		this.views = views.plus(view);
	}

	@Override
	public Optional<DOC> getDoc() {
		return Optional.ofNullable(doc);
	}

	@Override
	public PList<View<DOC>> getViews() {
		return views;
	}
}
