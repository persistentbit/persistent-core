package com.persistentbit.core.language;

import java.util.Objects;
import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 2/02/2017
 */
@FunctionalInterface
public interface Translator extends Function<Msg,Msg> {

    default Translator orTry(Translator trans){
        Translator self = this;
        return msg -> {
            Msg newMsg = self.apply(msg);
            if(Objects.equals(newMsg.getLocale(),msg.getLocale()) == false){
                return trans.apply(msg);
            }
            return newMsg;
        };
    }

}
