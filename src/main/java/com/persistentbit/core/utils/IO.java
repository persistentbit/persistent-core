package com.persistentbit.core.utils;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.exceptions.RtIOException;
import com.persistentbit.core.logging.PLog;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * General IO Utilities
 *
 * @author pmu
 * @since 28/10/2016
 */
public final class IO{

  private static final PLog log = PLog.get(IO.class);

  /**
   * copy data from in to out. <br>
   * When done, closes in but leaves out open
   *
   * @param in  The input stream to read from
   * @param out The destination output stream
   *
   * @throws IOException When error occurred while reading or writing
   */
  public static void copy(InputStream in, OutputStream out) throws IOException {
	try {
	  byte[] buffer = new byte[1024 * 10];
	  while(true) {
		int c = in.read(buffer);
		if(c == -1) {
		  break;
		}
		out.write(buffer, 0, c);
	  }
	} finally {
	  in.close();
	}

  }

  /**
   * Reads a text file
   *
   * @param f The file to read
   *
   * @return String with content of the text file
   */
  public static Optional<String> readFile(File f) {

	try {
	  if(f.exists() && f.isFile() && f.canRead()) {
		return readStream(new FileReader(f));
	  }
	  else {
		return Optional.empty();
	  }
	} catch(Exception e) {
	  throw new RuntimeException(e);
	}
  }

  private static Optional<String> readStream(Reader fin) throws IOException {

	try {
	  StringBuilder stringBuffer = new StringBuilder();
	  int           c;
	  char[]        buffer       = new char[1024];
	  do {
		c = fin.read(buffer);
		if(c != -1) {
		  stringBuffer.append(buffer, 0, c);
		}
	  }
	  while(c != -1);
	  return Optional.of(stringBuffer.toString());
	} finally {
	  fin.close();
	}
  }


  /**
   * Split a String into lines and supply each line to a String consumer
   *
   * @param txt     The text to convert
   * @param handler The line consumer
   */
  public static void lines(String txt, Consumer<String> handler) {
	lines(new StringReader(txt), handler);
  }

  public static PList<String> lines(String text) { return lines(new StringReader(text));}

  /**
   * Read text lines from a Reader source and supply each line to a line consumer
   *
   * @param r       The reader
   * @param handler The line consumer
   */
  public static void lines(Reader r, Consumer<String> handler) {
	try(BufferedReader bin = new BufferedReader(r)) {
	  while(true) {
		String line = bin.readLine();
		if(line == null) {
		  return;
		}
		handler.accept(line);
	  }
	} catch(IOException e) {
	  throw new RuntimeException("Error reading stream", e);
	}
  }

  /**
   * Read a text file ans split it into lines.<br>
   * Supply each line to a String consumer
   *
   * @param file    The text file
   * @param handler The line consumer
   */
  public static void lines(File file, Consumer<String> handler) {
	try(Reader in = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
	  lines(in, handler);
	} catch(IOException e) {
	  RtIOException.map("Error reading file " + file.getAbsolutePath(), e);
	}

  }
  /**
   * Return a list of lines read from a text File
   *
   * @param file The file to read
   *
   * @return a PList with all the lines
   */
  public static PList<String> lines(File file){
	List<String> res = new ArrayList<>();
	lines(file,s ->  res.add(s));
	PList<String> pList = PList.empty();
	return pList.plusAll(res);
  }

  /**
   * Return a list of lines read from a Reader
   *
   * @param reader The source reader
   *
   * @return a PList with all the lines
   */
  public static PList<String> lines(Reader reader) {
	List<String> res = new ArrayList<>();
	lines(reader,s ->  res.add(s));
	PList<String> pList = PList.empty();
	return pList.plusAll(res);
  }

  /**
   * Write a string to a file
   *
   * @param text The text to write
   * @param f    The file to write to
   */
  public static void writeFile(String text, File f) {
	try {
	  try(FileWriter fileOut = new FileWriter(f)) {
		fileOut.write(text);
	  }
	} catch(Exception e) {
	  throw new RuntimeException(e);
	}
  }


  /**
   * Convert a path to the system by replacing slashes with
   * the current platform specific slash type
   *
   * @param path The path to convert
   *
   * @return The converted path
   */
  public static String pathToSystemPath(String path) {
	path = path.replace('\\', File.separatorChar);
	path = path.replace('/', File.separatorChar);
	return path;
  }


}
