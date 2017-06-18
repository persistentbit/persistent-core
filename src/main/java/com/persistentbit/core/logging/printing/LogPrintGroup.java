package com.persistentbit.core.logging.printing;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.entries.LogEntry;

/**
 * A LogPrintGroup contains a list of {@link LogPrint} printers.<br>
 * Printing is done by executing the print of all added {@link LogPrint} instances.
 *
 * @author Peter Muys
 * @since 18/01/2017
 */
public class LogPrintGroup implements LogPrint{

    private PList<LogPrint> items;

    public LogPrintGroup(PList<LogPrint> items) {
        this.items = items;
    }

    public LogPrintGroup() {
        this(PList.empty());
    }

    @Override
    public LogPrint add(LogPrint other) {
        return new LogPrintGroup(items.plus(other));
    }

    @Override
    public void print(LogEntry logEntry) {
        items.forEach(i -> {
            try {
                i.print(logEntry);
            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public void print(Throwable exception) {
        items.forEach(i -> {
            try{
                i.print(exception);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }
}