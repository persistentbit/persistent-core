package com.persistentbit.core.logging.printing;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.entries.LogEntry;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 18/01/2017
 */
public class LogOutGroup implements LogOut{
    private PList<LogOut> items;

    public LogOutGroup(PList<LogOut> items) {
        this.items = items;
    }
    public LogOutGroup(){
        this(PList.empty());
    }

    @Override
    public LogOut add(LogOut other){
        return new LogOutGroup(items.plus(other));
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