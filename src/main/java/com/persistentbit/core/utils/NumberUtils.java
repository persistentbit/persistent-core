package com.persistentbit.core.utils;

import com.persistentbit.core.Result;
import com.persistentbit.core.logging.Log;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 28/12/2016
 */
public final class NumberUtils {

    public static Result<Integer> parsInt(String str){
        return Log.function(str).code(l -> {
            if(str == null){
                return Result.failure("string is null");
            }
            try{
                return Result.success(Integer.parseInt(str));
            }catch (RuntimeException e){
                return Result.failure(e);
            }
        });

    }
}
