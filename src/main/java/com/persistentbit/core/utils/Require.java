package com.persistentbit.core.utils;

import java.util.function.Supplier;

/**
 * Helpful functions to check if something required is ok.<br>
 */
public final class Require {
    //*********  isTrue
    public static boolean isTrue(boolean value){
        return isTrue(value,"Expected a 'true' value");
    }
    public static boolean isTrue(boolean value, Supplier<RuntimeException> exception){
        if(value == false){
            throwError(exception);
        }
        return true;
    }
    public static boolean isTrue(boolean value, String message){
        return isTrue(value,() -> new RuntimeException(message));
    }
    //*********  isFalse
    public static boolean isFalse(boolean value){
        return isFalse(value,"Expected a 'true' value");
    }
    public static boolean isFalse(boolean value, Supplier<RuntimeException> exception){
        if(value){
            throwError(exception);
        }
        return false;
    }
    public static boolean isFalse(boolean value, String message){
        return isFalse(value,() -> new RuntimeException(message));
    }

    private static void throwError(Supplier<RuntimeException> exception){
        throw exception.get();
    }
}
