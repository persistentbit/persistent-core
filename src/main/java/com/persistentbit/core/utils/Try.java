package com.persistentbit.core.utils;

import java.util.function.Function;

/**
 * Utility class for exception handling.
 * @since 19/10/16
 * @author Peter Muys
 */
public class Try {

    @FunctionalInterface
    public interface TryRun{
        void run() throws Exception;
    }
    @FunctionalInterface
    public interface TryRunGet<T>{
        T run() throws  Exception;
    }

    static public void run(TryRun run){
        run(run,(e) -> new RuntimeException(e));
    }
    static public void run(TryRun run, Function<Exception,? extends RuntimeException> ex){
        try{
            run.run();
        }catch (Exception e){
            throw ex.apply(e);
        }
    }

    static public <T> T runGet(TryRunGet<T> runGet){
        return runGet(runGet,(e) -> new RuntimeException(e));
    }

    static public <T> T runGet(TryRunGet<T> runGet, Function<Exception,? extends RuntimeException> ex){
        try{
            return runGet.run();
        }catch (Exception e){
            throw ex.apply(e);
        }
    }
}
