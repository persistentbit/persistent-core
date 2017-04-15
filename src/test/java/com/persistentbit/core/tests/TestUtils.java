package com.persistentbit.core.tests;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.io.*;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestData;
import com.persistentbit.core.tests.utils.TestValue;
import com.persistentbit.core.utils.UNumber;
import com.persistentbit.core.utils.UString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 5/01/17
 */
public class TestUtils{

	static final TestCase resolveResourceNameTest = TestCase.name("Resolve resourceName").code(tr -> {
		tr.isEquals(IO.resolveResourceName("/cv.city", "test/../hello").orElseThrow(), "/cv.city/hello");
		tr.isEquals(IO.resolveResourceName("/cv.city", "././test/../hello").orElseThrow(), "/cv.city/hello");
		tr.isFailure(IO.resolveResourceName("/cv.city", "../../test/../hello"));

	});

	static final TestCase copyTest = TestCase.name("copy").code(t -> {
		t.isFailure(IOCopy.copy(null, null));
		t.isFailure(IOCopy.copy(new ByteArrayInputStream(new byte[0]), null));
		t.isSuccess(
			IOCopy.copy(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream())
				  .verify((ByteArrayOutputStream bout) -> t.runNoException(() -> {
					bout.close();
					return bout.toByteArray().length == 0;
				}))
		);

		PStream.sequence(0).limit(1000).forEach(i -> {
			byte[]               bytesIn = TestData.createRandomBytes(15000);
			ByteArrayInputStream in      = new ByteArrayInputStream(bytesIn);
			t.isSuccess(IOCopy.copy(in, new ByteArrayOutputStream())
							  .verify((ByteArrayOutputStream bout) -> t.runNoException(() -> {
									bout.close();
									return compareBytes(bout.toByteArray(), bytesIn);
								}))
			);

		});
	});
	static final TestCase readText = TestCase.name("Reading Text").code(t -> {
		Charset charset = Charset.defaultCharset();
		t.isFailure(IORead.readTextFile(null, charset));
		t.isFailure(IORead.readTextFile(File.listRoots()[0], charset));
		t.isSuccess(TestData.createRandomTextFile("readTextFileTest", charset, 10)
							.verify(fileAndString -> {
								String readString = IORead.readTextFile(fileAndString._1,charset).orElseThrow();
								return readString.equals(fileAndString._2);
							})
		);

		t.isSuccess(TestData.createRandomTextFile("readTextFileTest", charset, 10)
							.verify(fileAndString -> {
								String readString = IOStreams.fileToInputStream(fileAndString._1)
															 .flatMap(is -> IOStreams.inputStreamToReader(is,charset))
															 .flatMap(ir -> IORead.readTextStream(ir)).orElseThrow();
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
		tr.isFailure(UNumber.numberToBigDecimal(null));
		Comparator<Number> nc = UNumber.numberComparator;
		tr.isEquals(nc.compare(1, 1.0), 0);
		tr.isEquals(nc.compare((short) 2, 2.0f), 0);
		tr.isEquals(nc.compare(1234567l, 1234567), 0);
		tr.isEquals(nc.compare(1234567l, 1234566), 1);
		tr.isFailure(UNumber.numberToBigDecimal(null));
		tr.isEquals(UNumber.numberToBigDecimal(1234), UNumber.numberToBigDecimal(1234.0));
		tr.isFailure(UNumber.parseBigDecimal(null));
		tr.isFailure(UNumber.parseInt(null));
		tr.isFailure(UNumber.parseLong(null));
		tr.isEquals(UNumber.parseLong("1234").orElseThrow(), 1234l);
	});


	static final TestCase testStringUtils = TestCase.name("Test UString utilities").code(tr -> {
		Function<String,String> supplier = name -> {
			switch(name){
				case "NAME": return "Peter";
				case "PATH": return "/home";
				default: return "";
			}
		};
		Function<String,String> replacer = UString.replaceDelimited("\\$","[0-9a-zA-Z_]+","",supplier);
		tr.isEquals(replacer.apply("Hello $NAME!"),"Hello Peter!");
	});

	public void testAll() {
		CoreTest.runTests(TestUtils.class);
	}

	public static void main(String... args) throws Exception {
		new TestUtils().testAll();
	}
}
