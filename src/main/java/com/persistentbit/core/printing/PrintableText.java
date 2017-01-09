package com.persistentbit.core.printing;

import com.persistentbit.core.logging.Log;
import com.persistentbit.core.utils.IO;

import java.io.StringWriter;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 9/01/2017
 */
@FunctionalInterface
public interface PrintableText{

    void print(PrintTextWriter out);

    default String printToString(){
        return Log.function().code(l -> {
            StringWriter stringWriter = new StringWriter();
            PrintTextWriter pw = new PrintTextWriter(stringWriter);
            print(pw);
            pw.flush();
            return stringWriter.toString();
        });
    }


    static PrintableText indent(String indentString, boolean indentFirstLine, PrintableText pt) {
        return printer -> {
            printer.flush();
            PrintTextWriter pw = new PrintTextWriter(IO.createIndentFilterWriter(printer,indentString,indentFirstLine));
            pt.print(pw);
            pw.flush();
        };
    }

    static PrintableText indent(PrintableText pt) {
        return indent("\t", true, pt);
    }

    static PrintableText fromString(String value) {
        return ps -> ps.print(value);
    }

    PrintableText empty = ps -> {};

    static PrintableText from(Object value) {
        if(value instanceof String) {
            return fromString(value.toString());
        }
        if(value instanceof PrintableText) {
            return (PrintableText) value;
        }
        return fromString("" + value);
    }
}
