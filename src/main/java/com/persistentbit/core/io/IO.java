package com.persistentbit.core.io;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.UString;

import java.io.File;
import java.io.FilterWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * General IO Utilities
 *
 * @author Peter Muys
 * @since 28/10/2016
 */
public final class IO {

    public static final Charset utf8 = Charset.forName("UTF-8");


	public static FilterWriter  createIndentFilterWriter(Writer writer, String indentString,boolean indentFirstLine, String newLineString){
        return IOFiles.createFilterWriter(writer,new Function<String,String>(){

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

	public static Result<URI> asURI(URL url){
		return Result.function(url).code(l -> Result.noExceptions(url::toURI));
	}
	public static Result<URI> asURI(String uri){
		return Result.function(uri).code(l -> {
			if(uri == null) {
				return Result.failure("uri is null");
			}
			return Result.success(new URI(uri));
		});
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

	public static Result<Path> asPath(URL url){
		return Result.function(url).code(l -> asURI(url).flatMap(IO::asPath));
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
	 * Replace all environment vars in a string using the patterns::
	 * <ul>
	 *     <li>${name}</li>
	 *     <li>$name</li>
	 *     <li>%name%</li>
	 * </ul>
	 * Non matchin names are replaced with an empty value
	 * @param source The String to expaned
	 * @return The expanded string.
	 */
    public static String replaceEnvVars(String source){
    	Function<String,String> sysEnv = name -> {
    		String value = System.getenv(name);
    		return value == null ? "" : value;
		};
    	return UString.replaceDelimited("\\$\\{",".+","\\}",sysEnv)
			.andThen(s-> UString.replaceDelimited("\\$","[a-zA-Z_0-9]+","",sysEnv).apply(s))
			.andThen(s -> UString.replaceDelimited("%",".+","%",sysEnv).apply(s))
		  	.apply(source);
	}

    public static void main(String... args) throws Exception {
        Result<PList<Path>> files = IOFiles.getAllFiles("${FENIKS_HOME}/**/devdocs/src/main/**/*.ddoc");
        ModuleCore.consoleLogPrint.print(files.getLog());
        files.orElseThrow();
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
}
