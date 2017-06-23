package com.persistentbit.core.printing;

import com.persistentbit.core.result.Result;

import java.io.*;
import java.nio.file.Path;

/**
 * PrintWriter to print out {@link PrintableText}.
 *
 * @author Peter Muys
 * @since 9/01/2017
 * @see PrintableText
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

    public static Result<PrintTextWriter> fromPath(Path path){
        return Result.noExceptions(() -> new PrintTextWriter(path.toFile()));
    }

    public static Result<Path> writeAndClose(Path path, PrintableText printableText){
        return Result.function(path,printableText).code(l -> fromPath(path)
			.flatMap(pw -> {
				try(PrintWriter out = pw){
					out.print(printableText);
				}
				return Result.success(path);
			}));
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
