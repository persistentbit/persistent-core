package com.persistentbit.core.utils;

import com.persistentbit.core.result.Result;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * PrintStream with indent.
 *
 * @author Peter Muys
 * @since 20/12/2016
 * @see IndentOutputStream
 */
public class IndentPrintStream extends PrintStream{
    private final IndentOutputStream indentOutputStream;

    private IndentPrintStream(IndentOutputStream out, Charset encoding) throws UnsupportedEncodingException {
        super(Objects.requireNonNull(out), true, Objects.requireNonNull(encoding).name());
        this.indentOutputStream = out;
    }

    public static Result<IndentPrintStream> of(IndentOutputStream out, Charset encoding){
        return  Result.noExceptions(()-> new IndentPrintStream(out,encoding));
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
        IndentPrintStream ips = IndentOutputStream.of(System.out)
                .flatMap(os -> IndentPrintStream.of(os,Charset.defaultCharset()))
                .orElseThrow();
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
