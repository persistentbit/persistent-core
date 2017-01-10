package com.persistentbit.core.printing;

import java.io.*;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 9/01/2017
 */
public class PrintTextWriter extends PrintWriter{
    public PrintTextWriter(Writer out) {
        super(out);
    }

    public PrintTextWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public PrintTextWriter(OutputStream out) {
        super(out);
    }

    public PrintTextWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public PrintTextWriter(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public PrintTextWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public PrintTextWriter(File file) throws FileNotFoundException {
        super(file);
    }

    public PrintTextWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    public void print(PrintableText printableText) {
        printableText.print(this);
    }
    public void println(PrintableText printableText) {
        printableText.print(this);
        println();
    }

    public void indent(PrintableText printableText) {
        print(PrintableText.indent(printableText));
    }
}
