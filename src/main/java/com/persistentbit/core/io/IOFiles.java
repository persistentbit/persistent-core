package com.persistentbit.core.io;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.OK;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.TimeMeasurement;
import com.persistentbit.core.utils.UNamed;
import com.persistentbit.core.utils.UNumber;
import com.persistentbit.core.utils.UString;

import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for working with Files
 *
 * @author petermuys
 * @since 15/04/17
 */
public final class IOFiles{

	/**
     * Write a string to a file
     *
     * @param text The text to write
     * @param f    The file to write to
     * @param charset Character encoding
     */
    public static Result<OK> write(String text, File f, Charset charset) {
    	return Result.function(UString.present(text,10),f,charset).code(l -> {
    		if(text == null){
    			return Result.failure("text is null");
			}
			return IOStreams.fileToWriter(f,charset)
				.flatMapExc(fileOut -> {
					try(Writer writer = fileOut){
						writer.write(text);
						return OK.result;
					}
				});
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

	public static FilterWriter createFilterWriter(Writer writer, Function<String, String> stringFilter){
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
		return mkdirsIfNotExisting(rootPath)
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
	 * @param fileName The full filename
	 * @return The result extension
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
			List<Path> files = new ArrayList<>();
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
		return p -> getFileNameExtension(p.getFileName().toString()).mapEmpty(e -> "").orElseThrow().equals(ext);
	}

	/**
	 * Create a filename Matcher.<br>
	 *     '*' matches anything except '/' or '\'<br>
	 *     '**' matches anything<br>
	 *     '?' matches any 1 character<br>
	 * @param matchExpr The filename expression
	 * @return A filename {@link Predicate}
	 */
	public static Predicate<String> fileNameMatcher(String matchExpr){
		matchExpr = pathToSystemPath(matchExpr);
		String reg = matchExpr.replace("\\","\\\\");
		reg = reg.replace(".","\\.");
		reg = reg.replace("?",".");
		reg = reg.replace("*","[^\\/]*?");
		reg = reg.replace("[^\\/]*?[^\\/]*?",".*?");
		reg = "^" + reg +"$";
		return UNamed.namedPredicate("FileNameMatcher[" + reg + "]", Pattern.compile(reg).asPredicate());
	}

	/**
	 * Calls {@link #getAllFiles(Path, String)} with the current directory as root.<br>
	  * @param matchPath The file matcher expression
	 * @return The {@link TimeMeasurement.Result} of all paths found
	 * @see #getAllFiles(Path, String)
	 */
	public static Result<PList<Path>> getAllFiles(String matchPath){
		return getAllFiles(Paths.get(""),matchPath);
	}


	/**
	 * Convert a {@link Path} to a real path
	 * @param p The path to convert
	 * @param options {@link LinkOption}
	 * @return The real path
	 * @see Path#toRealPath(LinkOption...)
	 */
	public static Result<Path> toRealPath(Path p,LinkOption...options){
		return Result.function(p).code(l ->
			Result.success(p.toRealPath(options))
		);
	}


	/**
	 * Find all files relative to a root path, where the name is matched by a search expression.<br>
	 * Environment variables are expanded using the method IO#replaceEnvVars.<br>
	 *
	 * @param root The root of the search
	 * @param matchPath The match expression
	 * @return The {@link Result} with all paths matching
	 * @see #fileNameMatcher(String)
	 * @see IO#replaceEnvVars(String)
	 */
	public static Result<PList<Path>> getAllFiles(Path root, String matchPath){
		return Result.function(root, matchPath).code(l -> {
			String resolved = pathToSystemPath(IO.replaceEnvVars(matchPath));
			l.info("resolved = " + resolved);
			Matcher m      = Pattern.compile("^[^*?]*\\" + File.separatorChar ).matcher(resolved);
			String  prefix ="";
			if(m.find()) {
				prefix = m.group();
			}
			l.info("prefix = " + prefix);
			String search = resolved.substring(prefix.length());
			//if(search.startsWith("**") == false){
			//	search = "**" + search;
			//}
			l.info("search = " + search);
			//Path current = Paths.get("").toRealPath();
			//l.info("Current = " + current);
			String finalSearch = Paths.get(root.toString(),prefix).toString() + File.separatorChar +  search;
			l.info("finalSearch = " + finalSearch);
			Predicate<String>  strFilter = fileNameMatcher(finalSearch);
			Predicate<Path> filter = UNamed.namedPredicate(search, (Path path) ->{
					String pathStr = IOFiles.toRealPath(path).orElseThrow().toString();
					return strFilter.test(pathStr);
			});
			l.info("base: " + root.resolve(prefix));
			l.info("Filter: " + strFilter);
			Path rootWithPrefix = root.resolve(prefix);
			if(Files.exists(rootWithPrefix) == false) {
				return Result.success(PList.empty());
			}
			return findPathsInTree(rootWithPrefix,filter);
		});

	}


}
