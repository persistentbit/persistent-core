package com.persistentbit.core.utils;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * PrintStream with indent.
 *
 * @author Peter Muys
 * @since 20/12/2016
 * @see IndentOutputStream
 */
public class IndentPrintStream extends PrintStream{
    private IndentOutputStream indentOutputStream;
    public IndentPrintStream(IndentOutputStream out) {
        super(out,true);
        this.indentOutputStream = out;
    }

    public IndentPrintStream(IndentOutputStream out, String encoding) throws UnsupportedEncodingException {
        super(out, true, encoding);
        this.indentOutputStream = out;
    }

    public IndentPrintStream indent() {
        flush();
        indentOutputStream.indent();
        return this;
    }
    public IndentPrintStream outdent() {
        flush();
        indentOutputStream.outdent();
        return this;
    }

    public static void main(String... args) throws Exception {
        IndentPrintStream ips = new IndentPrintStream(
                new IndentOutputStream(System.out)
        );
        System.setOut(ips);
        System.out.println("Test");
        ips.indent();
        System.out.println("indent1");
        ips.indent();
        System.out.println("indent2");
        ips.outdent();
        System.out.println("outdent1");
        ips.outdent();
        System.out.println("outdent2");


    }
}
