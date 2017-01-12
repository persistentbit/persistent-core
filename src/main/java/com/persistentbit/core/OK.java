package com.persistentbit.core;

import com.persistentbit.core.result.Result;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 9/01/2017
 */
public class OK {
    public static final OK         inst   = new OK();
    public static final Result<OK> result = Result.success(OK.inst);

    private OK() { }

    @Override
    public String toString() {
        return "OK";
    }
}
