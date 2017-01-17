package com.persistentbit.core.logging.entries;

import com.persistentbit.core.ModuleCore;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 10/01/2017
 */
public abstract class AbstractLogEntry implements LogEntry{


    @Override
    public String toString() {
		return ModuleCore.createLogPrinter(false).printableLog(this).printToString();
	}


}
