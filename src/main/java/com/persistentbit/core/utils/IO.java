package com.persistentbit.core.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * TODOC
 *
 * @author pmu
 * @since 28/10/2016
 */
public class IO {



    /**
     * copy data from in to out. <br>
     * When done, closes in but leaves out open
     *
     * @param in
     * @param out
     * @throws IOException
     */
    static public void copy(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024 * 10];
        try
        {
            while (true)
            {
                int c = in.read(buffer);
                if (c == -1)
                {
                    break;
                }
                out.write(buffer, 0, c);
            }
        }
        finally
        {
            in.close();
        }

    }

    /**
     * TODOC
     * @param f
     * @return
     */
    static public Optional<String> readFile(File f)
    {

        try
        {
            if (f.exists() && f.isFile() && f.canRead())
            {
                return readStream(new FileReader(f));
            }
            else
            {
                return Optional.empty();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static Optional<String> readStream(Reader fin) throws IOException
    {

        char[] buffer = new char[1024];
        try
        {
            StringBuffer stringBuffer = new StringBuffer();
            int c = 0;
            do
            {
                c = fin.read(buffer);
                if (c != -1)
                {
                    stringBuffer.append(buffer, 0, c);
                }
            }
            while (c != -1);
            return Optional.of(stringBuffer.toString());
        }
        finally
        {
            fin.close();
        }
    }

    static public class LinesCollector implements Consumer<String> {
        public final List<String> result = new ArrayList<>();
        @Override
        public void accept(String s)
        {
            result.add(s);
        }
    }

    static public void lines(String txt, Consumer<String> handler){
        lines(new StringReader(txt),handler);
    }

    static public void lines(File file, Consumer<String> handler)
    {
        try (Reader in = new InputStreamReader(new FileInputStream(file), "UTF-8"))
        {
            lines(in,handler);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error reading file " + file.getAbsolutePath(), e);
        }

    }

    static public void lines(Reader r,Consumer<String> handler){
        try (BufferedReader bin = new BufferedReader(r))
        {
            while (true)
            {
                String line = bin.readLine();
                if (line == null)
                {
                    return;
                }
                handler.accept(line);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error reading stream", e);
        }
    }

    static public void writeFile(String text, File f)
    {
        try
        {
            FileWriter fout = new FileWriter(f);
            try
            {
                fout.write(text);
            }
            finally
            {
                fout.close();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    static public String pathToSystemPath(String path){
        path.replace('\\',File.separatorChar);
        path.replace('/', File.separatorChar);
        return path;
    }
}
