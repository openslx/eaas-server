package de.bwl.bwfla.common.logging;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class LoggerFactory {
    @Produces @Dependent
    public static Logger produceStandardLogger(InjectionPoint injectionPoint) {
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }
    
    @Produces @Dependent
    @Typed(PrefixLogger.class)
    public static PrefixLogger producePrefixLogger(InjectionPoint injectionPoint) {
        return new PrefixLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }
}
