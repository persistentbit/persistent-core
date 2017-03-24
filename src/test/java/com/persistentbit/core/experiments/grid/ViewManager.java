package com.persistentbit.core.experiments.grid;

import com.persistentbit.core.collections.PList;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 24/03/2017
 */
public interface ViewManager<DOC> {

    DOC getDoc();
    PList<DOC>   getViews();

}
