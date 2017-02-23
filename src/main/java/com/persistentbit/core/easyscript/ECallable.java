package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.StrPos;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 23/02/2017
 */
@FunctionalInterface
public interface ECallable {
    public EEvalResult apply(EvalContext context, StrPos pos, PList<Object> args);
}
