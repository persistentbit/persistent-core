package com.persistbit.core;

import com.persistbit.core.utils.TestValue;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.logging.LogPrinter;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestData;
import com.persistentbit.core.testing.TestRunner;
import com.persistentbit.core.utils.IO;
import com.persistentbit.core.utils.NumberUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Comparator;

/**
 * TODOC
 *
 * @author petermuys
 * @since 5/01/17
 */
public class TestUtils{

	static final TestCase copyTest = TestCase.name("copy").code(t -> {
		t.isFailure(IO.copy(null, null));
		t.isFailure(IO.copy(new ByteArrayInputStream(new byte[0]), null));
		t.isSuccess(
			IO.copy(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream())
				.verify((ByteArrayOutputStream bout) -> t.runNoException(() -> {
					bout.close();
					return bout.toByteArray().length == 0;
				}))
		);

		PStream.sequence(0).limit(1000).forEach(i -> {
			byte[]               bytesIn = TestData.createRandomBytes(15000);
			ByteArrayInputStream in      = new ByteArrayInputStream(bytesIn);
			t.isSuccess(IO.copy(in, new ByteArrayOutputStream())
								.verify((ByteArrayOutputStream bout) -> t.runNoException(() -> {
									bout.close();
									return compareBytes(bout.toByteArray(), bytesIn);
								}))
			);

		});
	});
	static final TestCase readText = TestCase.name("Reading Text").code(t -> {
		Charset charset = Charset.defaultCharset();
		t.isFailure(IO.readTextFile(null, charset));
		t.isFailure(IO.readTextFile(File.listRoots()[0], charset));
		t.isSuccess(TestData.createRandomTextFile("readTextFileTest", charset, 10)
							.verify(fileAndString -> {
								String readString = IO.readTextFile(fileAndString._1,charset).orElseThrow();
								return readString.equals(fileAndString._2);
							})
		);

		t.isSuccess(TestData.createRandomTextFile("readTextFileTest", charset, 10)
							.verify(fileAndString -> {
								String readString = IO.fileToInputStream(fileAndString._1)
									.flatMap(is -> IO.inputStreamToReader(is,charset))
									.flatMap(ir -> IO.readTextStream(ir)).orElseThrow();
								return readString.equals(fileAndString._2);
							})
		);
	});

	static final TestCase baseValueClass = TestCase.name("BaseValue").code(tr -> {
		TestValue t1 = new TestValue(1234, "userX");
		TestValue t2 = new TestValue(1234, "userX");
		TestValue t3 = new TestValue(5678, "userY");
		System.out.println(t1 + ",   " + t2 + ",  " + t3);
		tr.isTrue(t1.hashCode() == t2.hashCode());
		tr.isTrue(t1.hashCode() != t3.hashCode());
		tr.isEquals(t1,t2);
		tr.isTrue(t1.equals(t3) == false);
		TestValue t4 = new TestValue(5678, "userY", 1);
		tr.isTrue(t3.equals(t4));
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


	static final TestCase testNumberUtils = TestCase.name("Test Number Utilities").code(tr -> {
		tr.isFailure(NumberUtils.numberToBigDecimal(null));
		Comparator<Number> nc = NumberUtils.numberComparator;
		tr.isEquals(nc.compare(1, 1.0), 0);
		tr.isEquals(nc.compare((short) 2, 2.0f), 0);
		tr.isEquals(nc.compare(1234567l, 1234567), 0);
		tr.isEquals(nc.compare(1234567l, 1234566), 1);
		tr.isFailure(NumberUtils.numberToBigDecimal(null));
		tr.isEquals(NumberUtils.numberToBigDecimal(1234), NumberUtils.numberToBigDecimal(1234.0));
		tr.isFailure(NumberUtils.parseBigDecimal(null));
		tr.isFailure(NumberUtils.parseInt(null));
		tr.isFailure(NumberUtils.parseLong(null));
		tr.isEquals(NumberUtils.parseLong("1234").orElseThrow(), 1234l);
	});



	public void testAll() {
		TestRunner.runAndPrint(TestUtils.class);
	}

	public static void main(String... args) throws Exception {
		LogPrinter.consoleInColor().registerAsGlobalHandler();
		new TestUtils().testAll();
	}
}
