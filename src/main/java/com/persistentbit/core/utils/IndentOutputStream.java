package com.persistentbit.core.utils;

import com.persistentbit.core.result.Result;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
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
    private IndentOutputStream(OutputStream out,Function<String,String> indent, Function<String,String> outdent) {
        super(Objects.requireNonNull(out));
        this.doIndent = Objects.requireNonNull(indent);
        this.doOutdent = Objects.requireNonNull(outdent);
    }



    static public Result<IndentOutputStream> of(OutputStream out,Function<String,String> indent, Function<String,String> outdent){
        return Result.noExceptions(()->new IndentOutputStream(out,indent,outdent));
    }
    static public Result<IndentOutputStream> of(OutputStream out, String indentString){
        return of(out,s -> s + indentString, s -> s.substring(0,s.length()-indentString.length()));
    }
    static public Result<IndentOutputStream> of(OutputStream out){
        return of(out,"\t");
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

}
