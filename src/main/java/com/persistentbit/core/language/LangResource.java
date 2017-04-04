package com.persistentbit.core.language;

import com.persistentbit.core.doc.Support;
import com.persistentbit.core.tuples.Tuple2;

import java.util.Locale;
import java.util.Objects;

/**
 * A LangResource is a {@link FunctionalInterface} that returns a Translation of a text with a {@link Locale}.<br>
 *
 * @author Peter Muys
 * @since 2/02/2017
 */
@Support
@FunctionalInterface
public interface LangResource {
    Tuple2<Locale,String> getTranslation(Locale loc, String text);

    default Translator translator(Locale loc){
        return msg -> {
            Locale msgLocale = msg.getLocale().orElse(null);
            if(Objects.equals(msgLocale,loc)){
                return msg;
            }
            Tuple2<Locale,String> res = this.getTranslation(loc,msg.getText());
            if(res._1.equals(msgLocale)){
                return msg;
            }
            return msg.withText(res._1,res._2);
        };
    }

    default LangResource orElse(LangResource langRes){
        LangResource self = this;
        return (loc,text) -> {
            Tuple2<Locale,String> selfRes = getTranslation(loc,text);
            if(Objects.equals(selfRes._1,loc)){
                return selfRes;
            }
            return langRes.getTranslation(loc,text);
        };
    }
}
