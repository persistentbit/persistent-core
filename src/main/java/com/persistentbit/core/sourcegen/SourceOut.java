package com.persistentbit.core.sourcegen;
import java.io.PrintWriter;

/**
 * @author Peter Muys
 * @since 22/12/2015
 */
public class SourceOut
{

    private PrintWriter wout;
    private boolean blockOpenSameLine;
    private String indent = "";
    private boolean needIndentPrint = false;

    public SourceOut(PrintWriter out, boolean blockOpenSameLine)
    {
        this.wout = out;
        this.blockOpenSameLine = blockOpenSameLine;
    }

    public SourceOut indent()
    {
        indent += "\t";
        return this;
    }

    public SourceOut outdent()
    {
        indent = indent.substring(1);
        return this;
    }

    public SourceOut bs()
    {
        if (blockOpenSameLine)
        {
            println(" {");
        }
        else
        {
            println();
            println("{");
        }
        indent += "\t";
        return this;
    }

    public SourceOut be(String be)
    {
        indent = indent.substring(1);
        return println(be);
    }
    public SourceOut be(){
        return be("}");
    }


    public SourceOut print(Object obj)
    {
        try
        {
            if (needIndentPrint)
            {
                wout.print(indent);
                needIndentPrint = false;
            }
            if (obj == null)
            {
                obj = "null";
            }
            wout.print(obj.toString());
            return this;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public SourceOut println(Object obj)
    {
        print(obj);
        return println();
    }

    public SourceOut println()
    {
        try
        {
            wout.println();
            needIndentPrint = true;
            return this;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}