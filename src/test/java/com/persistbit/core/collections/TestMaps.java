package com.persistbit.core.collections;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.collections.*;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;

import java.util.*;

/**
 * @author Peter Muys
 * @since 9/07/16
 */
public class TestMaps{


	public static final TestCase pmapTest = TestCase.name("Test PMap/POrderedMap add/remove").code(t -> {
		t.isSuccess(doAddRemove(PMap.empty()));
		t.isSuccess(doAddRemove(POrderedMap.empty()));
	});



	public void testAll() {
		TestRunner.runAndPrint(TestMaps.class);
	}



	private static Result<Nothing> doAddRemove(IPMap<Integer, String> empty) {
		return Result.function(empty).code(l -> {
			Map<Integer, String>   refMap = new LinkedHashMap<>();
			IPMap<Integer, String> pmap   = empty;
			Random                 r      = new Random(System.currentTimeMillis());
			int                    count  = 5000;
			l.info("Adding " + count + " elements to a PMap and a java Map");
			for(int t = 0; t < count; t++) {
				int    key = r.nextInt();
				String val = String.valueOf(r.nextGaussian());
				refMap.put(key, val);
				pmap = pmap.put(key, val);
			}
			l.info("Adding " + count + " elements to a PMap and a java Map...done");
			refMap.put(null, "1234");
			pmap = pmap.put(null, "1234");
			Set<Integer>     refKeys     = refMap.keySet();
			PStream<Integer> pstreamKeys = pmap.keys();
			//System.out.println(pstreamKeys);
			PSet<Integer> psetKeys = pstreamKeys.pset();
			//System.out.println(psetKeys);
			Set<Integer> pKeys = psetKeys.toSet();
			assert refMap.size() == pmap.size();

			if(pmap instanceof POrderedMap) {
				//Lets check if the order is ok...
				Iterator<Integer> refIter = refMap.keySet().iterator();
				Iterator<Integer> pIter   = pmap.keys().iterator();
				while(refIter.hasNext() && pIter.hasNext()) {
					assert Objects.equals(refIter.next(), pIter.next());

				}
				assert refIter.hasNext() == pIter.hasNext();
			}

			if(refKeys.equals(pKeys) == false) {
				throw new RuntimeException();
			}

			for(Map.Entry<Integer, String> entry : refMap.entrySet()) {
				if(pmap.get(entry.getKey()).equals(entry.getValue()) == false) {
					throw new RuntimeException(entry.toString());
				}
			}
			for(Map.Entry<Integer, String> entry : pmap.map().entrySet()) {
				if(pmap.get(entry.getKey()).equals(entry.getValue()) == false) {
					throw new RuntimeException(entry.toString());
				}
			}

			return Result.success(Nothing.inst);
		});
	}


}
