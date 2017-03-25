package com.persistentbit.core.experiments.grid;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 24/03/2017
 */
public interface View<DOC> {
    ViewManager<DOC> getManager();
    void docUpdated(DOC doc);

}
