package com.persistentbit.core.io;

import com.persistentbit.core.OK;
import com.persistentbit.core.result.Result;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TODOC
 *
 * @author petermuys
 * @since 15/04/17
 */
public class IOCopy{

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
		return IOStreams.closeAfter(in,()->Result.function().code(l -> {
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
	 * Copy the source {@link Path} to the destination path.
	 * @param source The source Path
	 * @param dest The destination Path
	 * @param options the {@link CopyOption} options
	 * @return The destination path
	 */
	public static Result<Path> copy(Path source, Path dest,CopyOption...options){
		return Result.function(source,dest).code(l ->
			Result.success(Files.copy(source,dest,options))
		);
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
			  IOStreams.closeAfter(out,()-> copy(in,out)).flatMap(out2 -> OK.result)
		);
	}

	/**
	 * copy data from in to a output path <br>
	 * When done, closes in and out
	 * @param in The InputStream
	 * @param out The output Path
	 * @return OK when copy and close succeeded
	 */
	public static Result<OK> copyAndClose(InputStream in, Path out){
		return Result.function(in,out).code(l ->
			copyAndClose(in,new FileOutputStream(out.toFile()))
		);
	}
}
