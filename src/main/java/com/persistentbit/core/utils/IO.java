package com.persistentbit.core.utils;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.OK;
import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Failure;
import com.persistentbit.core.result.Result;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * General IO Utilities
 *
 * @author Peter Muys
 * @since 28/10/2016
 */
public final class IO {

    public static final Charset utf8 = Charset.forName("UTF-8");




    /**
     * copy data from in to out. <br>
     * When done, closes in but leaves out open
     *
     * @param in  The input stream to read from (NOT NULL)
     * @param out The destination output stream (NOT NULL)
     * @param <T> Type of the output stream
     * @return Result of Output stream
     * @see #copyAndClose(InputStream, OutputStream)
     */
    public static <T extends OutputStream> Result<T> copy(InputStream in, T out) {
        return closeAfter(in,()->Result.function().code(l -> {
            l.params(in, out);
            if (in == null) {
                return Result.failure("in parameter can't be null");
            }
            if (out == null) {
                return Result.failure("out parameter can't be null");
            }
            byte[] buffer = new byte[1024 * 10];
            while (true) {
                int c = in.read(buffer);
                if (c == -1) {
                    break;
                }
                out.write(buffer, 0, c);
            }
            return Result.success(out);
        }));
    }

    /**
     * copy data from in to out. <br>
     * When done, closes in and out
     * @param in The InputStream
     * @param out The OutputStream
     * @param <T> The type of the OutputStream
     * @return OK when copy and close succeeded
     */
    public static <T extends OutputStream> Result<OK> copyAndClose(InputStream in, T out){
        return Result.function().code(l ->
              closeAfter(out,()-> copy(in,out)).flatMap(out2 -> OK.result)
        );
    }

    /**
     * Closes a Closable, mapping a thrown IO Exception to a {@link Failure}
     * @param closeable The closeable (nullable)
     * @return OK if success or Failure on Exception
     */
    public static Result<OK> close(Closeable closeable){
        return Result.function().code(l -> {
            if(closeable == null){
                return OK.result;
            }
            closeable.close();
            return OK.result;
        });
    }

    /**
     * Executes some code returning a {@link Result}
     * and then closes the provided {@link Closeable}.<br>
     * If the close failed, then the result is mapped to a {@link Failure}
     * @param closeable The Closable to close after the code
     * @param before The code to run before the close
     * @param <T> The type of the Result
     * @return The Result of the before code combined with the closing
     */
    public static <T> Result<T> closeAfter(Closeable closeable, Supplier<Result<T>> before){
        return Result.function().code(l -> {
                Result<T> res = before.get();
                return res.match(
                    onSuccess -> close(closeable).flatMap(ok -> onSuccess),
                    onEmpty -> close(closeable).flatMap(ok -> onEmpty),
                    onFailure -> close(closeable).flatMap(ok -> onFailure)
                );
        });
    }

    public static Result<FileInputStream> fileToInputStream(File f) {
        return Log.function(f).code(l -> {
            if (f == null) {
                return Result.failure("File is null");
            }
            if (f.exists() == false) {
                return Result.failure("File does not exist:" + f);
            }
            if (f.isFile() == false) {
                return Result.failure("Not a file: " + f);
            }
            if (f.canRead() == false) {
                return Result.failure("No read access: " + f);
            }
            return Result.success(new FileInputStream(f));
        });
    }

    public static Result<Reader> inputStreamToReader(InputStream in, Charset charset) {
        return Result.function().code(l -> {
            if (in == null) {
                return Result.failure("Inputstream is null");
            }
            return Result.success(new InputStreamReader(in, charset));
        });
    }


    public static Result<Reader> fileToReader(File f, Charset charset) {
        return fileToInputStream(f).flatMap(is -> inputStreamToReader(is, charset)).logFunction(f,charset);
    }


    /**
     * Read a Reader stream into a String.<br>
     * The given Reader is automatically closed.<br>
     *
     * @param fin the input Reader
     * @return The String content from the Reader
     */
    public static Result<String> readTextStream(Reader fin) {
        return Result.function().code(l -> {
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
                return Result.failure(e);
            } finally {
                IO.close(fin).orElseThrow();
            }
        });

    }

    /**
     * Read all bytes from an InputStream and closes the inputStream when done
     *
     * @param in The inputStream
     *
     * @return a Result of {@link PByteList}
     */
    public static Result<PByteList> readBytes(InputStream in) {
        return Result.function(in).code(l -> {
            if(in == null) {
                return Result.failure("in is null");
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            return copyAndClose(in, bout)
                .map(ok -> PByteList.from(bout.toByteArray()));
        });
    }


    /**
     * Read an InputStream into a String.<br>
     * Uses UTF-8 for encoding.<br>
     * The given stream is automatically closed.<br>
     *
     * @param fin the inputStream
     * @param charset The character encoding
     * @return The String content from the InputStream
     */
    public static Result<String> readTextStream(InputStream fin,Charset charset) {
        return Result.function().code(l -> {
            if (fin == null) {
                return Result.failure("Inputstream is null");
            }
            if (charset == null) {
                return Result.failure("Charset is null");
            }
            return inputStreamToReader(fin,charset)
                    .flatMapNoSuccess((r,e) ->
                        close(fin).flatMap(ok -> r)
                    )
                    .flatMap(IO::readTextStream);
        });

    }

    /**
     * Reads a text file
     *
     * @param f The file to read
     * @param charset The character encoding
     * @return String with content of the text file
     */
    public static Result<String> readTextFile(File f,Charset charset) {
        return Result.function(f).code(l ->
                fileToReader(f,charset).flatMap(IO::readTextStream)
        );
    }


    public static Result<PList<String>> readLines(String text) {
		return Result.function(UString.present(text, 40)).code(l -> {
			if (text == null) {
                return Result.empty();
            }
            return readLinesFromReader(new StringReader(text));
        });

    }


    public static Result<PList<String>> readLinesFromReader(Reader r) {
        return Result.function().code(l -> {
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
                return Result.failure(new RuntimeException("Error reading lines from Reader stream", e));
            }
        });

    }

    public static Result<PList<String>> readLinesFromFile(File file,Charset charset) {
        return Result.function(file,charset).code(l -> fileToReader(file,charset).flatMap(IO::readLinesFromReader));
    }


    public static Result<FileOutputStream> fileToOutputStream(File f){
        return fileToOutputStream(f,false).logFunction(f);
    }
    public static Result<FileOutputStream> fileToOutputStream(File f, boolean append){
        return Result.function(f).code(l -> {
            if(f == null){
                return Result.failure("File is null");
            }
            return Result.success(new FileOutputStream(f,append));
        });
    }

    public static Result<Writer> outputStreamToWriter(OutputStream out,Charset charset){
        return Result.function(out,charset).code(l-> {
            if(out == null){
                return Result.failure("Outputstream is null");
            }
            if(charset == null){
                return Result.failure("Charset is null");
            }
            return Result.success(new OutputStreamWriter(out,charset));
        });
    }
    public static Result<Writer> fileToWriter(File f, Charset charset){
        return fileToWriter(f, charset, false).logFunction(f, charset);
    }
    public static Result<Writer> fileToWriter(File f, Charset charset, boolean append){
        return fileToOutputStream(f,append).flatMap(os-> outputStreamToWriter(os,charset)).logFunction(f,charset,append);
    }

    /**
     * Write a string to a file
     *
     * @param text The text to write
     * @param f    The file to write to
     * @param charset Character encoding
     */
    public static void writeFile(String text, File f,Charset charset) {
        Log.function(f).code(l -> {
            Objects.requireNonNull(text,"text is null");
            try (Writer fileOut = fileToWriter(f,charset).orElseThrow()) {
                fileOut.write(text);
                return Nothing.inst;
            }
        });

    }

    public static Result<File> mkdirsIfNotExisting(File f) {
        return Result.function(f).code(log -> {
            if(f == null) {
                return Result.failure("File is null");
            }
            if(f.exists()) {
                if(f.isDirectory() == false) {
                    return Result.failure("File is not a directory:" + f.getAbsolutePath());
                }
                log.info("Dir already exists:" + f.getAbsolutePath());
                return Result.success(f);
            }
            if(f.mkdirs() == false) {
                return Result.failure("mkdirs() returned false for " + f.getAbsolutePath());
            }
            log.info("Dir created: " + f.getAbsolutePath());
            return Result.success(f);
        });
    }

    public static FilterWriter  createFilterWriter(Writer writer, Function<String, String> stringFilter){
        Objects.requireNonNull(writer,"writer");
        Objects.requireNonNull(stringFilter,"stringFilter");
        return new FilterWriter(writer) {
            @Override
            public void write(int c) throws IOException {
                out.append(stringFilter.apply(Character.toString((char)c)));
            }

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                out.append(stringFilter.apply(new String(cbuf,off,len)));
            }

            @Override
            public void write(String str, int off, int len) throws IOException {
                out.append(stringFilter.apply(str.substring(off,off + len)));
            }
        };
    }

    public static FilterWriter  createIndentFilterWriter(Writer writer, String indentString,boolean indentFirstLine, String newLineString){
        return createFilterWriter(writer,new Function<String,String>(){

            private boolean prevNl = indentFirstLine;
            @Override
            public String apply(String s) {
                if(s.isEmpty()){
                    return s;
                }
                if(prevNl){
                    s = indentString + s;
                }
                prevNl = s.endsWith(newLineString);
                if(prevNl){
                    s = s.substring(0,s.length()- newLineString.length());
                    return s.replace(newLineString, newLineString + indentString) + newLineString;
                }
                return s.replace(newLineString,newLineString +  indentString);

            }
        });
    }
    public static FilterWriter  createIndentFilterWriter(Writer writer, String indentString,boolean indentFirstLine){
        return createIndentFilterWriter(writer,indentString,indentFirstLine,System.lineSeparator());
    }


    /**
     * Convert a path to the system by replacing slashes with
     * the current platform specific slash type
     *
     * @param path The path to convert
     * @return The converted path
     */
    public static String pathToSystemPath(String path) {
        if (path == null) {
            return null;
        }
        path = path.replace('\\', File.separatorChar);
        path = path.replace('/', File.separatorChar);
        return path;
    }

    public static Result<File> createDayFile(File rootPath, String prefix, String postfix){
        if(rootPath == null){
            return Result.<File>failure("rootPath is null").logFunction(rootPath,prefix,postfix);
        }
        if(prefix == null){
            return Result.<File>failure("prefix is null").logFunction(rootPath,prefix,postfix);
        }
        if(postfix == null){
            return Result.<File>failure("postfix is null").logFunction(rootPath,prefix,postfix);
        }
        return IO.mkdirsIfNotExisting(rootPath)
                .map(rp ->
                        new File(rp,prefix + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + postfix)
                ).logFunction(rootPath,prefix,postfix);

    }

    /**
     * Get the System Temp folder
     *
     * @return The temp folder result
     *
     * @see #createTempDir(String)
     */
    public static Result<File> getSystemTempDir() {
        return Result.success(new File(System.getProperty("java.io.tmpdir")))
            .verify(f -> f.exists(), "Temp directory does not exist")
            .verify(f -> f.canWrite(), "Can't write in Temp directory")
            .logFunction();
    }

	public static Result<File> getUserHomeDir() {
		return Result.success(new File(System.getProperty("user.home")))
			.verify(f -> f.exists(), "User home directory does not exist")
			.verify(f -> f.canWrite(), "Can't write to the user home directory")
			.logFunction();
	}



    /**
     * Create a new folder in the system Temp folder
     *
     * @param baseName The base name for the temp folder
     *
     * @return The new temp folder {@link File}
     *
     * @see #getSystemTempDir()
     */
    public static Result<File> createTempDir(String baseName) {
        return getSystemTempDir()
            .flatMap(tmpBase ->
                         Result.noExceptions(() ->
                                                 Files.createTempDirectory(tmpBase.toPath(), baseName).toFile())
            );
    }

    /**
     * Delete a directory with all it's content
     *
     * @param dirToDelete The directory to delete
     *
     * @return Ok result
     */
    public static Result<OK> deleteDirRecursive(File dirToDelete) {
        return Result.function(dirToDelete).code(l -> {
            if(dirToDelete == null) {
                return Result.failure("dirToDelete is null");
            }
            if(dirToDelete.isDirectory() == false) {
                return Result.failure("Not a directory:" + dirToDelete);
            }

            Files.walk(dirToDelete.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .peek(f -> l.info("Deleting " + f))
                .forEach(File::delete);
            return OK.result;
        });
    }

    /**
     * Read a class path resource as a String
     * @param classPathResource The resource name
     * @param charset The char encoding
     * @return The String result
     */
    public static Result<String> readClassPathResource(String classPathResource, Charset charset) {
        return Result.function(classPathResource, charset).code(l-> {
            if(classPathResource == null){
                return Result.failure("classPathResource is null");
            }
            if(charset == null){
                return Result.failure("charset is null");
            }
            return getClassPathResourceReader(classPathResource, charset)
                .flatMap(IO::readTextStream);
        });
    }

    public static Result<InputStream> getClassPathResourceStream(String classPathResource) {
        return Result.function(classPathResource).code(l -> {
            if(classPathResource == null) {
                return Result.failure("classPathResource is null");
            }
            InputStream in = IO.class.getResourceAsStream(classPathResource);
            if(in == null){
                return Result.failure("Classpath resource not found:" + classPathResource);
            }
            return Result.success(in);
        });
    }

    public static Result<Reader> getClassPathResourceReader(String classPathResource, Charset charset) {
        return Result.function(classPathResource).code(l -> {
            if(classPathResource == null) {
                return Result.failure("classPathResource is null");
            }
            if(charset == null) {
                return Result.failure("charset is null");
            }
            return getClassPathResourceStream(classPathResource)
                .flatMap(stream -> inputStreamToReader(stream, charset));
        });
    }


    public static Result<Properties> readClassPathProperties(String classPathResource, Charset charset) {
        return Result.function(classPathResource, charset).code(l -> {
            if(classPathResource == null) {
                return Result.failure("classPathResource is null");
            }
            if(charset == null) {
                return Result.failure("charset is null");
            }
            return getClassPathResourceReader(classPathResource, charset)
                .flatMap(IO::readProperties);

        });

    }

    /**
     * Try reading a properties file from a Reader, closing the Reader when finished.<br>
     *
     * @param reader The Reader for the properties. Automatically closed
     *
     * @return The Result {@link Properties}
     */
    public static Result<Properties> readProperties(Reader reader) {
        return Result.function(reader).code(l -> {
            if(reader == null) {
                return Result.failure("reader is null");
            }
            Properties props = new Properties();

            return closeAfter(reader, () -> Result.noExceptions(() -> {
                props.load(reader);
                return props;
            }));
        });
    }


    public  static Result<String>  getReadableFileSize(File file){
        return Result.function(file).code(l -> {
            if(file == null){
                return Result.failure("file is null");
            }
            if(file.exists() == false){
                return Result.failure("File does not exist: " + file);
            }
            if(file.canRead() == false){
                return Result.failure("Can't read file: " + file);
            }
			return Result.success(UNumber.readableComputerSize(file.length()));
		});
    }

    /**
     * Convert an Url string to an URL
     *
     * @param urlString The String to convert
     *
     * @return A Result with an URL or a failure on error
     */
    public static Result<URL> asURL(String urlString) {
        return Result.function(urlString).code(l -> {
            if(urlString == null) {
                return Result.failure("path is null");
            }
            return Result.success(new URL(urlString));
        });
    }

    public static Result<URI> asURI(File file) {
        return Result.function(file).code(l -> {
            if(file == null) {
                return Result.failure("file is null");
            }
            return Result.noExceptions(file::toURI);
        });
    }
    public static Result<URI> asURI(Path p){
    	return Result.function(p).code(l -> {
    		if(p == null){
    			return Result.failure("Path is null");
			}
			return Result.noExceptions(p::toUri);
		});
	}

	public static Result<URL> asURL(File file) {
		return asURI(file).flatMap(uri -> Result.noExceptions(uri::toURL)).logFunction(file);
	}

	public static Result<URL> asURL(Path path) {
		return asURI(path).flatMap(uri -> Result.noExceptions(uri::toURL)).logFunction(path);
	}

	/**
	 * Convert an {@link URL} to a {@link File}
	 * @param url The URL
	 * @return The File or a failure
	 */
	public static Result<File> asFile(URL url){
		return Result.function(url).code(l-> {
			/*try {
				return Result.success(new File(url.toURI()));
			} catch(URISyntaxException e) {
				return Result.success(new File(url.getPath()));
			}*/
			return asFile(url.toURI());
		});
	}
	/**
	 * Convert an {@link URI} to a {@link File}
	 * @param uri The URI
	 * @return The File or a failure
	 */
	public static Result<File> asFile(URI uri){
		return asPath(uri).map(p -> p.toFile());

	}
	/**
	 * Convert an {@link URI} to a {@link Path}
	 * @param uri The URI
	 * @return The Path or a failure
	 */
	public static Result<Path> asPath(URI uri){
		return Result.function(uri).code(l-> Result.success(Paths.get(uri)));

	}
    /**
     * Resolve . and .. in a resource name
     * @param baseName The base URL in case the sub does not begin with a '/'
     * @param sub The name to resolve
     * @return The result resolved resource name
     */
    public static Result<String> resolveResourceName(String baseName, String sub) {
        return Result.function(baseName, sub).code(l -> {
            String all;
            if(sub.startsWith("/")) {
                all = sub;
            }
            else {
                all = baseName + "/" + sub;
            }
            List<String> elements = new ArrayList<>();
            for(String element : all.split("/")) {
                if(element.equals(".") || element.equals("")) {
                    continue;
                }
                if(element.equals("..")) {
                    if(elements.size() == 0) {
                        return Result.failure("Invalid: " + sub + ", not enough base parts in " + baseName);
                    }
                    elements.remove(elements.size() - 1);
                }
                else {
                    elements.add(element);
                }
            }
			return Result.success("/" + UString.join("/", elements));
		});
    }

	/**
	 * Get the mime type from a {@link URL}
	 * @param url the url
	 * @return The mimetype or a failure
	 */
	public static Result<String> getMimeType(URL url){
		return Result.function(url).code(l-> {
			URLConnection uc   = url.openConnection();
			String        type = uc.getContentType();
			return Result.result(type);
		});
	}

	/**
	 * Get the mimetype from a {@link File}
	 * @param file The File
	 * @return The mimetype or a failure
	 */
	public static Result<String> getMimeType(File file){
		return Result.function(file).code(l ->
			asURL(file).flatMap(IO::getMimeType)
		);

	}

	/**
	 * Get the filename extension.<br>
	 * The extension is the string after the last '.' character
	 * @param fileName The filename to get the extension from.
	 * @return error if filename is null empty if the file has no extension
	 */
	public static Result<String> getFileNameExtension(String fileName){
		return Result.function(fileName).code(l -> {
			if(fileName == null){
				return Result.failure("filename is null");
			}
			int i = fileName.lastIndexOf('.');
			if(i < 0){
				return Result.empty("No Extension for filename '" + fileName+"'");
			}
			return Result.success(fileName.substring(i+1));
		});
	}

	/**
	 * Removes the extension from a filename.<br>
	 * The extension is the string after the last '.' character
	 * @param fileName
	 * @return
	 */
	public static Result<String> getFileNameWithoutExtension(String fileName){
		return Result.function(fileName).code(l -> {
			if(fileName == null){
				return Result.failure("filename is null");
			}
			int i = fileName.lastIndexOf('.');
			if(i < 0){
				return Result.success(fileName);
			}
			return Result.success(fileName.substring(0,i));
		});
	}

	/**
	 * Find all the file {@link Path Paths} in a directory and subdirectory.<br>
	 * @param root	The root path
	 * @param filter A filter for each found file
	 * @return Result with a list of Paths
	 */
	public static Result<PList<Path>> findPathsInTree(Path root, Predicate<Path> filter){
		return Result.function(root,filter).code(l -> {
			List<Path>	files = new ArrayList<>();
			Files.walkFileTree(root,new SimpleFileVisitor<Path>(){
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if(filter.test(file)){
						files.add(file);
					}
					return FileVisitResult.CONTINUE;
				}
			});
			return Result.success(PList.from(files));
		});
	}

	/**
	 * A {@link Path} {@link Predicate} that filters on the filename extension
	 * @param ext The extension to include
	 * @return The Predicate
	 */
	public static Predicate<Path> fileExtensionPredicate(String ext){
		return p -> IO.getFileNameExtension(p.getFileName().toString()).mapEmpty(e -> "").orElseThrow().equals(ext);
	}

    public static void main(String... args) throws Exception {
        for(File f : new File("d:\\").listFiles()){
            if(f.isDirectory()){
                continue;
            }
            System.out.println(f.getName() + " " + f.length() + ", " + getReadableFileSize(f));
        }
    }
}
