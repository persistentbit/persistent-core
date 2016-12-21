package com.persistbit.core.logging;

import com.persistentbit.core.logging.LogCollector;

import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/12/2016
 */
public class JpaPersoonDAO {
    private final LogCollector logger;

    public JpaPersoonDAO(LogCollector logger) {
        this.logger = logger;
        logger.fun();
    }

    public Optional<Persoon> getPersoonOpt(int id){
        return Optional.empty();
    }

    public Persoon getPersoon(int id){
        return logger.fun(id).runl(log -> {
            return log.done(getPersoonOpt(id).get());
        });
    }
}
