package com.persistentbit.core.io;

import com.persistentbit.core.result.Result;

import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * TODOC
 *
 * @author petermuys
 * @since 15/04/17
 */
public class IOClassPath{

	/**
     * Read a class path resource as a String
     * @param classPathResource The resource name
     * @param charset The char encoding
     * @return The String result
     */
    public static Result<String> read(String classPathResource, Charset charset) {
        return Result.function(classPathResource, charset).code(l-> {
            if(classPathResource == null){
                return Result.failure("classPathResource is null");
            }
            if(charset == null){
                return Result.failure("charset is null");
            }
            return getReader(classPathResource, charset)
                .flatMap(IORead::readTextStream);
        });
    }

	public static Result<InputStream> getStream(String classPathResource) {
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

	public static Result<Reader> getReader(String classPathResource, Charset charset) {
        return Result.function(classPathResource).code(l -> {
            if(classPathResource == null) {
                return Result.failure("classPathResource is null");
            }
            if(charset == null) {
                return Result.failure("charset is null");
            }
            return getStream(classPathResource)
                .flatMap(stream -> IOStreams.inputStreamToReader(stream, charset));
        });
    }

	public static Result<Path> getPath(String classPathResource){
        return Result.function(classPathResource).code(l-> {
            if(classPathResource == null){
                return Result.failure("classPathResource is null");
            }
            URL url = IO.class.getResource(classPathResource);
            if(url == null){
                return Result.failure("Resource not found: " + classPathResource);
            }
            return IO.asPath(url);
        });
    }

}
