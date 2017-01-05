package com.persistentbit.core.utils;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;

import java.io.*;

/**
 * General IO Utilities
 *
 * @author Peter Muys
 * @since 28/10/2016
 */
public final class IO{


	/**
	 * copy data from in to out. <br>
	 * When done, closes in but leaves out open
	 *
	 * @param in  The input stream to read from (NOT NULL)
	 * @param out The destination output stream (NOT NULL)
	 *
	 * @Return Result of Output stream
	 */
	public static <T extends OutputStream> Result<T> copy(InputStream in, T out) {
		return Result.function().code(l -> {
			l.params(in, out);
			if(in == null) {
				return Result.failure("in parameter can't be null");
			}
			if(out == null) {
				return Result.failure("out parameter can't be null");
			}
			try {
				byte[] buffer = new byte[1024 * 10];
				while(true) {
					int c = in.read(buffer);
					if(c == -1) {
						break;
					}
					out.write(buffer, 0, c);
				}
				return Result.success(out);
			} finally {
				try {
					in.close();
				} catch(IOException e) {
					Result.failure(e);

				}

			}
		});


	}

	public static Result<FileInputStream> fileToInputStream(File f) {
		return Log.function(f).code(l -> {
			if(f == null) {
				return Result.failure("File is null");
			}
			if(f.exists() == false) {
				return Result.failure("File does not exist:" + f);
			}
			if(f.isFile() == false) {
				return Result.failure("Not a file: " + f);
			}
			if(f.canRead() == false) {
				return Result.failure("No read access: " + f);
			}
			return Result.success(new FileInputStream(f));
		});
	}

	public static Result<Reader> inputStreamToReader(InputStream in) {
		return Result.function().code(l -> {
			if(in == null) {
				return Result.failure("Inputstream is null");
			}
			return Result.success(new InputStreamReader(in));
		});
	}


	public static Result<Reader> fileToReader(File f) {
		return Log.function(f).code(l -> {
			if(f == null) {
				return Result.failure("File is null");
			}
			if(f.exists() == false) {
				return Result.failure("File does not exist:" + f);
			}
			if(f.isFile() == false) {
				return Result.failure("Not a file: " + f);
			}
			if(f.canRead() == false) {
				return Result.failure("No read access: " + f);
			}
			return Result.success(new FileReader(f));
		});
	}

	/**
	 * Read a Reader stream into a String.<br>
	 * The given Reader is automatically closed.<br>
	 *
	 * @param fin the input Reader
	 *
	 * @return The String content from the Reader
	 */
	public static Result<String> readTextStream(Reader fin) {
		return Log.function().code(l -> {
			if(fin == null) {
				return Result.failure("Reader is null");
			}

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
				return Result.success(stringBuffer.toString());
			} catch(IOException e) {
				return Result.failure(e);
			} finally {
				try {
					fin.close();
				} catch(IOException e) {
					Result.failure("Error closing stream: " + e.getMessage());
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
	 *
	 * @return The String content from the InputStream
	 */
	public static Result<String> readTextStream(InputStream fin) {
		return Log.function().code(l -> {
			if(fin == null) {
				return Result.failure("Inputstream is null");
			}
			return inputStreamToReader(fin)
				.flatMap(IO::readTextStream);
		});

	}

	/**
	 * Reads a text file
	 *
	 * @param f The file to read
	 *
	 * @return String with content of the text file
	 */
	public static Result<String> readTextFile(File f) {
		return Log.function(f).code(l ->
										fileToReader(f).flatMap(IO::readTextStream)
		);
	}


	public static Result<PList<String>> readLines(String text) {
		return Result.function(StringUtils.present(text, 40)).code(l -> {
			if(text == null) {
				return Result.empty();
			}
			return readLinesFromReader(new StringReader(text));
		});

	}


	public static Result<PList<String>> readLinesFromReader(Reader r) {
		return Log.function().code(l -> {
			if(r == null) {
				return Result.failure("Reader is null");
			}
			try(BufferedReader bin = new BufferedReader(r)) {
				PList<String> lines = PList.empty();
				while(true) {
					String line = bin.readLine();
					if(line == null) {
						break;
					}
					lines = lines.plus(line);
				}
				return Result.success(lines);
			} catch(IOException e) {
				return Result.failure(new RuntimeException("Error reading lines from Reader stream", e));
			}
		});

	}

	public static Result<PList<String>> readLinesFromFile(File file) {
		return Result.function(file).code(l -> fileToReader(file).flatMap(IO::readLinesFromReader));
	}


	/**
	 * Write a string to a file
	 *
	 * @param text The text to write
	 * @param f    The file to write to
	 */
	public static void writeFile(String text, File f) {
		Log.function(StringUtils.present(text, 40), f).code(l -> {
			try(FileWriter fileOut = new FileWriter(f)) {
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
	 *
	 * @return The converted path
	 */
	public static String pathToSystemPath(String path) {
		if(path == null) {
			return null;
		}
		path = path.replace('\\', File.separatorChar);
		path = path.replace('/', File.separatorChar);
		return path;
	}


}
