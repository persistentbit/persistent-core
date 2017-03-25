package com.persistentbit.core.experiments.grid;

import com.persistentbit.core.collections.PList;

import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 24/03/2017
 */
public interface ViewManager<DOC> {

    Optional<DOC> 		getDoc();
    PList<View<DOC>>   	getViews();
	void addView(View<DOC> view);
}
