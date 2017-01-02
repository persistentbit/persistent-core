package com.persistbit.core.collections;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.logging.Logged;
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


	@Test
	public void testBase64() {
		testBin(PByteList::toBase64String, PByteList::fromBase64String);
	}

	@Test
	public void testHex() {
		testBin(PByteList::toHexString, PByteList::fromHexString);
	}

	private void testBin(Function<PByteList, String> toString, Function<String, PByteList> fromString) {
		Logged.function().log(l -> {
			PByteList bl = PByteList.from(getClass().getResourceAsStream("/programming_is_terrible.pdf"));
			l.debug("Got " + bl.size() + " bytes from pdf");
			String    value = toString.apply(bl);
			PByteList newBl = fromString.apply(value);
			Assert.assertArrayEquals(bl.toArray(), newBl.toArray());
			Assert.assertEquals(bl, newBl);
			return Nothing.inst;
		});

	}
}
