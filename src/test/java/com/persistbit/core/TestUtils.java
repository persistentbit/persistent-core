package com.persistbit.core;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.logging.LogPrinter;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestData;
import com.persistentbit.core.testing.TestRunner;
import com.persistentbit.core.utils.IO;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;

/**
 * TODOC
 *
 * @author petermuys
 * @since 5/01/17
 */
public class TestUtils{

	private static final TestCase copyTest = TestCase.name("copy").code(t -> {
		t.assertFailure(IO.copy(null, null));
		t.assertFailure(IO.copy(new ByteArrayInputStream(new byte[0]), null));
		t.assertSuccess(
			IO.copy(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream())
				.verify((ByteArrayOutputStream bout) -> t.runNoException(() -> {
					bout.close();
					return bout.toByteArray().length == 0;
				}))
		);

		PStream.sequence(0).limit(1000).forEach(i -> {
			byte[]               bytesIn = TestData.createRandomBytes(15000);
			ByteArrayInputStream in      = new ByteArrayInputStream(bytesIn);
			t.assertSuccess(IO.copy(in, new ByteArrayOutputStream())
								.verify((ByteArrayOutputStream bout) -> t.runNoException(() -> {
									bout.close();
									return compareBytes(bout.toByteArray(), bytesIn);
								}))
			);

		});
	});
	private static final TestCase readText = TestCase.name("Reading Text").code(t -> {
		Charset charset = Charset.defaultCharset();
		t.assertFailure(IO.readTextFile(null, charset));
		t.assertFailure(IO.readTextFile(File.listRoots()[0],charset));
		t.assertSuccess(TestData.createRandomTextFile("readTextFileTest",charset, 10)
							.verify(fileAndString -> {
								String readString = IO.readTextFile(fileAndString._1,charset).orElseThrow();
								return readString.equals(fileAndString._2);
							})
		);

		t.assertSuccess(TestData.createRandomTextFile("readTextFileTest",charset, 10)
							.verify(fileAndString -> {
								String readString = IO.fileToInputStream(fileAndString._1)
									.flatMap(is -> IO.inputStreamToReader(is,charset))
									.flatMap(ir -> IO.readTextStream(ir)).orElseThrow();
								return readString.equals(fileAndString._2);
							})
		);
	});


	private static boolean compareBytes(byte[] left, byte[] right) {
		if(left.length != right.length) {
			return false;
		}
		for(int t = 0; t < left.length; t++) {
			if(left[t] != right[t]) {
				return false;
			}
		}
		return true;
	}

	private static final TestCase ioTests = TestCase.name("IO Tests").info("Test for all IO functions").subTestCases(
		copyTest, readText
	);

	@Test
	public void testIO() {
		TestRunner.runTest(ioTests).orElseThrow();
	}

	public static void main(String... args) throws Exception {
		LogPrinter.consoleInColor().registerAsGlobalHandler();
		TestRunner.runTest(ioTests).orElseThrow();
	}
}
