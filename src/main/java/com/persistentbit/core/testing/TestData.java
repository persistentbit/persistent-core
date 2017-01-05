package com.persistentbit.core.testing;

import com.persistentbit.core.result.Result;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.IO;

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

	public static String createTestString(int maxSize) {
		return new String(createRandomBytes(maxSize), Charset.forName("UTF-8"));
	}

	public static Result<Tuple2<File, String>> createRandomTextFile(String namePrefix, int maxSize) {
		return Result.function().code(l -> {
			String txt = createTestString(maxSize);
			File   f   = File.createTempFile(namePrefix, ".txt");
			f.deleteOnExit();
			IO.writeFile(txt, f);
			return Result.success(Tuple2.of(f, txt));
		});

	}
}
