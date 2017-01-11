package com.persistentbit.core.logging.entries;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 10/01/2017
 */
public abstract class AbstractLogEntry implements LogEntry{


    @Override
    public String toString() {
        return getClass().getSimpleName()+ "[]";
    }


}
