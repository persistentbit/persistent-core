package com.persistentbit.core.testing;

import com.persistentbit.core.io.IOFiles;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.tuples.Tuple2;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Random;

/**
 * Util class to create test data of all sorts.
 *
 * @author petermuys
 * @since 5/01/17
 */
public class TestData{

	public static byte[] createRandomBytes(int maxSize) {
		Random r = new Random(System.currentTimeMillis());

		byte[] result = new byte[r.nextInt(maxSize)];
		r.nextBytes(result);
		return result;
	}

	public static String createTestString(Charset charset, int maxSize) {
		return new String(createRandomBytes(maxSize), charset);
	}

	public static Result<Tuple2<File, String>> createRandomTextFile(String namePrefix,Charset charset, int maxSize) {
		return Result.function().code(l -> {
			String txt = createTestString(charset,maxSize);
			File   f   = File.createTempFile(namePrefix, ".txt");
			f.deleteOnExit();
			IOFiles.write(txt, f,charset);
			return Result.success(Tuple2.of(f, txt));
		});

	}
}
