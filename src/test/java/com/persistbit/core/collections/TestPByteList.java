package com.persistbit.core.collections;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;

import java.util.function.Function;

/**
 * Unit test for {@link PByteList}
 *
 * @author petermuys
 * @since 9/11/16
 */
public class TestPByteList{

	static final TestCase base64Test = TestCase.name("PByteList Base64").code(tr -> {
		testBin(tr,PByteList::toBase64String, PByteList::fromBase64String);
	});
	static final TestCase hexTest    = TestCase.name("PByteList Hex").code(tr -> {
		testBin(tr, PByteList::toHexString, PByteList::fromHexString);
	});



	public void testAll() {
		TestRunner.runAndPrint(TestPByteList.class);
	}


	private static void testBin(TestRunner tr, Function<PByteList, String> toString, Function<String, PByteList> fromString) {
		Log.function().code(l -> {
			PByteList bl = PByteList.from(TestPByteList.class.getResourceAsStream("/programming_is_terrible.pdf"));
			l.info("Got " + bl.size() + " bytes from pdf");
			String    value = toString.apply(bl);
			PByteList newBl = fromString.apply(value);
			tr.isEquals(bl.toArray(), newBl.toArray());
			tr.isEquals(bl, newBl);
			return Nothing.inst;
		});

	}
}
