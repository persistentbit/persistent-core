package com.persistentbit.core.glasgolia.repl;

import com.persistentbit.core.collections.PList;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 7/03/2017
 */

public class GGReplCmd {
    public final String name;
    public final PList<Object> params;

    private GGReplCmd(String name, PList<Object> params) {
        this.name = name;
        this.params = params;
    }
    public GGReplCmd(String name, Object... params) {
        this(name,PList.val(params));
    }
}
