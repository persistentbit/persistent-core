package com.persistentbit.core.experiments.shell;

import java.util.function.Predicate;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 17/03/2017
 */

public interface VFileFilter extends Predicate<VFile>{
    VFileFilter all = vf -> true;


}
