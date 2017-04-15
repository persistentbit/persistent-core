package com.persistentbit.core.io;

import com.persistentbit.core.OK;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.result.Failure;
import com.persistentbit.core.result.Result;

import java.io.*;
import java.nio.charset.Charset;
import java.util.function.Supplier;

/**
 * TODOC
 *
 * @author petermuys
 * @since 15/04/17
 */
public class IOStreams{

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

	public static Result<Writer> outputStreamToWriter(OutputStream out, Charset charset){
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
}
