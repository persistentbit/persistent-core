package com.persistbit.core.collections;

import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.logging.PLog;
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

	private static final PLog log = PLog.get(TestPByteList.class);

	@Test
	public void testBase64() {
		testBin(PByteList::toBase64String, PByteList::fromBase64String);
	}

	@Test
	public void testHex() {
		testBin(PByteList::toHexString, PByteList::fromHexString);
	}

	private void testBin(Function<PByteList, String> toString, Function<String, PByteList> fromString) {
		PByteList bl = PByteList.from(getClass().getResourceAsStream("/programming_is_terrible.pdf"));
		log.info("Got " + bl.size() + " bytes from pdf");
		String    value = toString.apply(bl);
		PByteList newBl = fromString.apply(value);
		Assert.assertArrayEquals(bl.toArray(), newBl.toArray());
		Assert.assertEquals(bl, newBl);
	}
}
