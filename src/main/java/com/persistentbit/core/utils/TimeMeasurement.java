package com.persistentbit.core.utils;


import com.persistentbit.core.logging.PLog;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Utility class voor performance testing.<br>
 * Usage Example::<br>
 * TimeMeasurement duration = new TimeMeasurement("[Name of Operation]");<br>
 *    ...code...<br>
 * System.out.println(duration.done());<br>
 * in console: [Nmae of Operation] 1234ms<br>
 * <br>
 * <br>
 * Created by pmu on 19/12/2014.<br>
 */
public class TimeMeasurement{
    private final String    name;
    private final long      startTime;

    public TimeMeasurement(){
        this("");
    }

    public TimeMeasurement(String name) {
        startTime   =   System.nanoTime();
        this.name   =   name;
    }

    static<T> T runAndLog(PLog  log, String name,Supplier<T> code){
        TimeMeasurement tm = new TimeMeasurement(name);
        T result = code.get();
        log.info(tm.done().toString());
        return result;
    }

    static <T> T runAndLog(PLog log, Supplier<T> code){
        return runAndLog(log,"TimeMeasurement",code);
    }

    static void runAndLog(PLog log, String name, Runnable code){
        TimeMeasurement tm = new TimeMeasurement(name);
        code.run();
        log.info(tm.done().toString());

    }
    static void runAndLog(PLog log, Runnable code){
        runAndLog(log,"TimeMeasurement", code);
    }


    public interface Result {
        long   getDurationInNanos();
        long   getDurationInMs();
        String getName();
    }





    public Result done(){
        final long stopTime   =   System.nanoTime();
        return new Result(){
            @Override
            public long getDurationInNanos() {
                return stopTime-startTime;
            }

            @Override
            public long getDurationInMs() {
                return getDurationInNanos()/1000000;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String toString() {
                return  getName() + " " + getDurationInMs() + "ms";
            }
        };
    }
}
