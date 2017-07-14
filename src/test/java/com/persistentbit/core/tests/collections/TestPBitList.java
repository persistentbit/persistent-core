package com.persistentbit.core.tests.collections;

import com.persistentbit.core.collections.PBitList;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.tests.CoreTest;

import java.util.Random;

/**
 * TODOC
 *
 * @author petermuys
 * @since 14/07/17
 */
public class TestPBitList{

	static final TestCase testSetGet = TestCase.name("PBitList set and get").code(tr -> {
		Random r = new Random(System.currentTimeMillis());
		PList<Boolean> randList;
		for(int length=0; length<100; length++){
			//System.out.println("Length = " + length);
			randList = PList.empty();
			for(int t=0; t< length; t++){
				randList = randList.plus(r.nextBoolean());
			}
			PBitList bitList = PBitList.from(randList);
			tr.isEquals(bitList.size(),randList.size());
			//System.out.println(bitList.toBinaryString());
			tr.isEquals(bitList.toBinaryString(), randList.map(b -> b ? "1" : "0").toString(""));
			for(int t=0; t< length; t++){
				tr.isEquals(bitList.get(t), randList.get(t));
			}
		}
	});


	public void testAll() {
		CoreTest.runTests(TestPBitList.class);
	}


	public static void main(String[] args) {
		CoreTest.testLogPrint.registerAsGlobalHandler();
		new TestPBitList().testAll();
	}
}
