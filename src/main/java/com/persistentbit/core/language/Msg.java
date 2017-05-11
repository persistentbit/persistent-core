package com.persistentbit.core.language;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * A Msg instance defines a text in a {@link Locale} with optional extra parameters
 * that will be mixed in the text using a {@link MessageFormat} instance.<br>
 * If the {@link Locale} is not defined then the text is seen as a key value
 * that can be looked up somewhere else.
 */
public class Msg {
    static public Locale nlBE = new Locale("nl","BE");
    static public Locale nlNL = new Locale("nl","NL");

    private final String text;
    private final Object[] data;
    private final Locale    locale;

    private Msg(Locale locale, String text, Object...data) {
        this.locale = locale;
        this.text = Objects.requireNonNull(text);
        this.data = data;
    }

    public Msg withLocale(Locale locale){
        return locale(locale,text,data);
    }

    public Msg withText(String text){
        return locale(locale,text,data);
    }
    public Msg withText(Locale locale, String text){
        return locale(locale,text,data);
    }
    public String getText(){
        return text;
    }

    public Optional<Locale> getLocale() {
        return Optional.ofNullable(locale);
    }

    @Override
    public String toString() {
        MessageFormat messageFormat = new MessageFormat(text,locale);
        return messageFormat.format(data);
    }

    static private Msg locale(Locale locale, String msg, Object...values){
        return new Msg(locale,msg, values);
    }

    static public Msg en(String msg, Object...values){
        return new Msg(Locale.ENGLISH, msg, values);
    }
    static public Msg fr(String msg, Object...values){
        return new Msg(Locale.FRENCH, msg, values);
    }
    static public Msg nlBE(String msg, Object...values){
        return new Msg(nlBE,msg,values);
    }
    static public Msg nlNL(String msg, Object...values){
        return new Msg(nlNL,msg,values);
    }
    static public Msg key(String key, Object...values){
        return new Msg(null, key, values);
    }


    public static void main(String... args) throws Exception {
		Msg msg =
			Msg.nlBE("Hello, {0} it is now {1,time} at {2,date,short} of {2,date,full}", "Bravo", new Date(), new Date());
		System.out.println("msg:" + msg);
        System.out.println("msg:" + msg.withLocale(Msg.nlNL));
        System.out.println("msg:" + msg.withLocale(Locale.ENGLISH));
    }
}
