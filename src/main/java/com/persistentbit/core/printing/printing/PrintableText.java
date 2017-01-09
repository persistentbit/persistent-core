package com.persistentbit.core.printing.printing;

import com.persistentbit.core.logging.Log;
import com.persistentbit.core.utils.IO;

import java.io.StringWriter;
import java.util.function.Consumer;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 9/01/2017
 */
@FunctionalInterface
public interface PrintableText extends Consumer<PrintTextWriter> {


    default String printToString(){
        return Log.function().code(l -> {
            StringWriter stringWriter = new StringWriter();
            PrintTextWriter pw = new PrintTextWriter(stringWriter);
            accept(pw);
            pw.flush();
            return stringWriter.toString();
        });
    }



    static PrintableText inset(String indentString, boolean indentFirstLine, PrintableText pt){
        return printer -> {
            printer.flush();
            PrintTextWriter pw = new PrintTextWriter(IO.createIndentFilterWriter(printer,indentString,indentFirstLine));
            pt.accept(pw);
            pw.flush();
        };
    }

    static PrintableText inset(PrintableText pt){
        return inset("\t",true ,pt);
    }

}
