package com.persistentbit.core.utils;

import java.util.function.Supplier;

/**
 * Helpfull functions to check if something required is ok.<br>
 */
public class Require {
    //*********  isTrue
    static public boolean isTrue(boolean value){
        return isTrue(value,"Expected a 'true' value");
    }
    static public boolean isTrue(boolean value, Supplier<RuntimeException> exception){
        if(value == false){
            throwError(exception);
        }
        return value;
    }
    static public boolean isTrue(boolean value, String message){
        return isTrue(value,() -> new RuntimeException(message));
    }
    //*********  isFalse
    static public boolean isFalse(boolean value){
        return isFalse(value,"Expected a 'true' value");
    }
    static public boolean isFalse(boolean value, Supplier<RuntimeException> exception){
        if(value){
            throwError(exception);
        }
        return value;
    }
    static public boolean isFalse(boolean value, String message){
        return isFalse(value,() -> new RuntimeException(message));
    }

    static private void throwError(Supplier<RuntimeException> exception){
        RuntimeException e = exception.get();
        throw e;
    }
}
