package com.persistentbit.core.utils;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.Result;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.Logged;
import com.persistentbit.core.logging.LoggedException;

import java.io.*;
import java.nio.charset.Charset;

/**
 * General IO Utilities
 *
 * @author Peter Muys
 * @since 28/10/2016
 */
public final class IO {


    /**
     * copy data from in to out. <br>
     * When done, closes in but leaves out open
     *
     * @param in  The input stream to read from
     * @param out The destination output stream
     * @throws LoggedException When error occurred while reading or writing
     */
    public static Result<Nothing> copy(InputStream in, OutputStream out){
        return Logged.function().log(l -> {
            try {
                byte[] buffer = new byte[1024 * 10];
                while (true) {
                    int c = in.read(buffer);
                    if (c == -1) {
                        break;
                    }
                    out.write(buffer, 0, c);
                }
                return Result.success(Nothing.inst);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    l.debug("Exception while closing the inputstream");
                    throw e;
                }

            }
        });


    }

    /**
     * Reads a text file
     *
     * @param f The file to read
     * @return String with content of the text file
     */
    public static Result<String> readTextFile(File f) {
        return fileToReader(f).flatMap(IO::readTextStream);

    }

    public static Result<FileReader> fileToReader(File f) {
        return Logged.function(f).log(l -> {
            if (f == null) {
                return l.<FileReader>fail("File is null");
            }
            if (f.exists() == false) {
                return l.<FileReader>fail("File does not exist:" + f);
            }
            if (f.isFile() == false) {
                return l.<FileReader>fail("Not a file: " + f);
            }
            if (f.canRead() == false) {
                return l.<FileReader>fail("No read access: " + f);
            }
            try {
                return Result.success(new FileReader(f));
            } catch (FileNotFoundException e) {
                return l.<FileReader>fail("Can't create Reader for file:" + f, e);
            }
        });

    }

    /**
     * Read a Reader stream into a String.<br>
     * The given Reader is automatically closed.<br>
     *
     * @param fin the input Reader
     * @return The String content from the Reader
     */
    public static Result<String> readTextStream(Reader fin) {
        return Logged.function().log(l -> {
            if (fin == null) {
                return l.<String>fail("Reader is null");
            }
            try {
                StringBuilder stringBuffer = new StringBuilder();
                int c;
                char[] buffer = new char[1024];
                do {
                    c = fin.read(buffer);
                    if (c != -1) {
                        stringBuffer.append(buffer, 0, c);
                    }
                }
                while (c != -1);
                return Result.success(stringBuffer.toString());
            } catch (IOException e) {
                return l.<String>fail("Error reading text Reader stream", e);
            } finally {
                try {
                    fin.close();
                } catch (IOException e) {
                    l.<String>fail("Error closing stream: " + e.getMessage());
                }

            }
        });

    }

    /**
     * Read an InputStream into a String.<br>
     * Uses UTF-8 for encoding.<br>
     * The given stream is automatically closed.<br>
     *
     * @param fin the inputStream
     * @return The String content from the InputStream
     */
    public static Result<String> readTextStream(InputStream fin){
        return Logged.function().log(l -> {
            if(fin == null){
                return l.<String>fail("Inputstream is null");
            }
            return readTextStream(new InputStreamReader(fin, Charset.forName("UTF-8")));
        });

    }


    public static Result<PList<String>> readLines(String text) {
        if (text == null) {
            return Result.empty();
        }
        return readLinesFromReader(new StringReader(text));
    }


    public static Result<PList<String>> readLinesFromReader(Reader r) {
        return Logged.function().log(l -> {
            if (r == null) {
                return l.<PList<String>>fail("Reader is null");
            }
            try (BufferedReader bin = new BufferedReader(r)) {
                PList<String> lines = PList.empty();
                while (true) {
                    String line = bin.readLine();
                    if (line == null) {
                        break;
                    }
                    lines = lines.plus(line);
                }
                return Result.success(lines);
            } catch (IOException e) {
                return l.<PList<String>>fail("Error reading lines from Reader stream", e);
            }
        });

    }

    public static Result<PList<String>> readLinesFromFile(File file) {
        return Logged.function(file).log(l -> fileToReader(file).flatMap(IO::readLinesFromReader));
    }



    /**
     * Write a string to a file
     *
     * @param text The text to write
     * @param f    The file to write to
     */
    public static void writeFile(String text, File f) {
        Logged.function(StringUtils.present(text,40),f).log(l -> {
            try (FileWriter fileOut = new FileWriter(f)) {
                fileOut.write(text);
                return Nothing.inst;
            }
        });

    }


    /**
     * Convert a path to the system by replacing slashes with
     * the current platform specific slash type
     *
     * @param path The path to convert
     * @return The converted path
     */
    public static String pathToSystemPath(String path) {
        if(path == null){
            return null;
        }
        path = path.replace('\\', File.separatorChar);
        path = path.replace('/', File.separatorChar);
        return path;
    }


}
