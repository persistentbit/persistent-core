package com.persistentbit.core.utils;

import com.persistentbit.core.Result;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.exceptions.RtIOException;

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
     * @throws RtIOException When error occurred while reading or writing
     */
    public static void copy(InputStream in, OutputStream out) throws RtIOException {
        try {
            byte[] buffer = new byte[1024 * 10];
            while (true) {
                int c = in.read(buffer);
                if (c == -1) {
                    break;
                }
                out.write(buffer, 0, c);
            }
        } catch (IOException e) {
            throw new RtIOException("IO error while copying stream.", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                System.err.println("Error closing stream: " + e.getMessage());
            }

        }

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

    public static Result<Reader> fileToReader(File f) {
        if (f == null) {
            return Result.failure("File is null");
        }
        if (f.exists() == false) {
            return Result.failure("File does not exist:" + f);
        }
        if (f.isFile() == false) {
            return Result.failure("Not a faile: " + f);
        }
        if (f.canRead() == false) {
            return Result.failure("No read access: " + f);
        }
        try {
            return Result.success(new FileReader(f));
        } catch (FileNotFoundException e) {
            return Result.failure("Can't create Reader for file:" + f, e);
        }
    }

    /**
     * Read a Reader stream into a String.<br>
     * The given Reader is automatically closed.<br>
     *
     * @param fin the input Reader
     * @return The String content from the Reader
     */
    public static Result<String> readTextStream(Reader fin) {
        if (fin == null) {
            return Result.failure("Reader is null");
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
            return Result.failure("Error reading text Reader stream", e);
        } finally {
            try {
                fin.close();
            } catch (IOException e) {
                System.out.println("Error closing stream: " + e.getMessage());
            }

        }
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
        if(fin == null){
            return Result.failure("Inputstream is null");
        }
        return readTextStream(new InputStreamReader(fin, Charset.forName("UTF-8")));
    }


    public static Result<PList<String>> readLines(String text) {
        if (text == null) {
            return Result.empty();
        }
        return readLinesFromReader(new StringReader(text));
    }


    public static Result<PList<String>> readLinesFromReader(Reader r) {
        if (r == null) {
            return Result.failure("Reader is null");
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
            return Result.failure("Error reading lines from Reader stream", e);
        }
    }

    public static Result<PList<String>> readLinesFromFile(File file) {
        return fileToReader(file).flatMap(IO::readLinesFromReader);
    }



    /**
     * Write a string to a file
     *
     * @param text The text to write
     * @param f    The file to write to
     */
    public static void writeFile(String text, File f) {
        try {
            try (FileWriter fileOut = new FileWriter(f)) {
                fileOut.write(text);
            }
        } catch (IOException e) {
            throw new RtIOException("Error while write a text to file " + f.getAbsolutePath(), e);
        }
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
