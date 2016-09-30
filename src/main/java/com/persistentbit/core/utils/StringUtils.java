package com.persistentbit.core.utils;

import com.persistentbit.core.NotNullable;

import java.util.Objects;

/**
 * General String utilities, because we all have to have our own  StringUtils version
 */
public class StringUtils {

    /**
     * Takes a raw string and converts it to a java code string:<br>
     * <ul>
     *     <li>tab to \t</li>
     *     <li>newline to \n</li>
     *     <li>cr to \r</li>
     *     <li>\ to \\</li>
     *     <li>backspace to \b</li>
     *     <li>" to \"</li>
     *     <li>\ to \'</li>
     * </ul>
     * @param s The unescaped string (can't be null)
     * @return The escaped string
     */
    static public String escapeToJavaString(String s){
        Objects.requireNonNull(s,"Can't escape a null string");
        StringBuilder sb = new StringBuilder(s.length()+4);
        for(int t=0; t<s.length();t++){
            char c = s.charAt(t);
            if(c == '\t'){
                sb.append("\\t");
            } else if(c == '\n'){
                sb.append("\\n");
            } else if(c == '\r'){
                sb.append("\\r");
            } else if(c == '\\'){
                sb.append('\\');
            } else if (c == '\b'){
                sb.append("\\b");
            } else if(c == '\"'){
                sb.append("\\\"");
            } else if(c == '\''){
                sb.append("\\\'");
            }else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Convert the first character in the given string to Uppercase.
     * @param s String to convert, can't be null
     * @return The new string with the first character in uppercase and the rest as it was.
     */
    static public String firstCapital(@NotNullable String s){
        Objects.requireNonNull(s);
        if(s.isEmpty()) { return s; }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
