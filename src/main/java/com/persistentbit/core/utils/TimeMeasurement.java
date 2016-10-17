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
 *
 *
 *
 * Created by pmu on 19/12/2014.<br>
 */
public class TimeMeasurement{
    private final String    name;
    private final long      startTime;

    /**
     * Create a new TimeMeasurement with an empty name
     */
    public TimeMeasurement(){
        this("");
    }

    /**
     * Create a new TimeMeasurement with a name.
     * @param name The name of the measurement
     */
    public TimeMeasurement(String name) {
        startTime   =   System.nanoTime();
        this.name   =   name;
    }

    /**
     * Run the measurement and log the time using info level to the given log
     * @param log   The Log
     * @param name  The name of the measurement
     * @param code The code to measure
     * @param <T> The return Type of the code
     * @return The return value of the code
     */
    static public <T> T runAndLog(PLog  log, String name,Supplier<T> code){
        TimeMeasurement tm = new TimeMeasurement(name);
        T result = code.get();
        log.info(tm.done().toString());
        return result;
    }

    /**
     * Run the measurement and log the time using info level to the given log.<br>
     * The name of the measurement is "TimeMeasurement".<br>
     * @param log The Log
     * @param code The code to measure
     * @param <T> The return Type of the code
     * @return The return value of the code
     */
    static public <T> T runAndLog(PLog log, Supplier<T> code){
        return runAndLog(log,"TimeMeasurement",code);
    }

    /**
     * Run a time measurement and logs the result to System.out.<br>
     * @param name The name of the measurement
     * @param code The code to measure
     */
    static public void runAndLog(String name, Runnable code){
        TimeMeasurement tm = new TimeMeasurement(name);
        code.run();
        System.out.println(tm.done().toString());

    }

    /**
     * Run a time measurement and logs the result to System.out with the name "TimeMeasurement".<br>
     * @param code The code to measure
     */
    static public void runAndLog(Runnable code){
        runAndLog("TimeMeasurement", code);
    }

    /**
     * Run a time measurement and logs the result to System.out.<br>
     * @param name The name of the measurement
     * @param code The code to measure
     * @param <T> The result value type.
     * @return The result value of the code
     */
    static public <T> T runAndLog(String name,Supplier<T> code){
        TimeMeasurement tm = new TimeMeasurement(name);
        T result = code.get();
        System.out.println(tm.done().toString());
        return result;
    }

    /**
     * Run a time measurement and logs the result to System.out with the name "TimeMeasurement".<br>
     * @param code The code to measure
     * @param <T> The code result type
     * @return The result value of the code
     */
    static public <T> T runAndLog(Supplier<T> code){
        return runAndLog("TimeMeasurement",code);
    }


    /**
     * The result of a measurement.<br>
     */
    public interface Result {
        /**
         *
         * @return The result in nano seconds
         */
        long   getDurationInNanos();

        /**
         *
         * @return The result in ms
         */
        long   getDurationInMs();

        /**
         *
         * @return The name of the measurement
         */
        String getName();
    }


    /**
     * Ends the measurement and return the result of the measurement.
     * @return  The result time.
     */
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
