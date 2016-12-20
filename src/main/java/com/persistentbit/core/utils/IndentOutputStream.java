package com.persistentbit.core.utils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

/**
 * Outputstream with possibility to add a prefix (indent) to each line (after each \n).
 *
 * @author Peter Muys
 * @since 20/12/2016
 */
public class IndentOutputStream extends FilterOutputStream{
    private String prefix = "";
    private final Function<String,String> doIndent;
    private final Function<String,String> doOutdent;
    private boolean prevNewLine = false;
    public IndentOutputStream(OutputStream out,Function<String,String> indent, Function<String,String> outdent) {
        super(out);
        this.doIndent = indent;
        this.doOutdent = outdent;
    }
    public IndentOutputStream(OutputStream out,String indendString) {
        this(out,s -> s + indendString, s -> s.substring(0,s.length()-indendString.length()));
    }
    public IndentOutputStream(OutputStream out){
        this(out,"\t");
    }
    public IndentOutputStream indent() {
        prefix = doIndent.apply(prefix);
        return this;
    }
    public IndentOutputStream outdent() {
        prefix = doOutdent.apply(prefix);
        return this;
    }

    @Override
    public void write(int b) throws IOException {
        if(prevNewLine){
            byte[] prefixBytes = prefix.getBytes();
            for(int t=0; t<prefixBytes.length; t++){
                super.write(prefixBytes[t]);
            }
            prevNewLine = false;
        }
        super.write(b);
        prevNewLine = b == '\n';
    }

    static public void main(String...args) throws Exception{

    }
}
