package com.persistbit.core.collections;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;

/**
 * Unit test for {@link PByteList}
 *
 * @author petermuys
 * @since 9/11/16
 */
public class TestPByteList{

	private static final TestCase base64Test = TestCase.name("PByteList Base64").code(t -> {
		testBin(PByteList::toBase64String, PByteList::fromBase64String);
	});
	private static final TestCase hexTest    = TestCase.name("PByteList Hex").code(t -> {
		testBin(PByteList::toHexString, PByteList::fromHexString);
	});
	private static final TestCase allTests   = TestCase.name("PByteAllTests").subTestCases(
		base64Test, hexTest
	);


	@Test
	public void testAll() {
		TestRunner.runTest(allTests).orElseThrow();
	}

	private static void testBin(Function<PByteList, String> toString, Function<String, PByteList> fromString) {
		Log.function().code(l -> {
			PByteList bl = PByteList.from(TestPByteList.class.getResourceAsStream("/programming_is_terrible.pdf"));
			l.info("Got " + bl.size() + " bytes from pdf");
			String    value = toString.apply(bl);
			PByteList newBl = fromString.apply(value);
			Assert.assertArrayEquals(bl.toArray(), newBl.toArray());
			Assert.assertEquals(bl, newBl);
			return Nothing.inst;
		});

	}
}
